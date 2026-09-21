package com.ontheway.service;

import com.ontheway.dto.request.ProcessRequestDto;
import com.ontheway.dto.response.ProcessResponseDto;
import com.ontheway.entity.Delivery;
import com.ontheway.entity.DeliveryOrder;
import com.ontheway.entity.FailedAndCancelled;
import com.ontheway.entity.Image;
import com.ontheway.entity.Product;
import com.ontheway.entity.Request;
import com.ontheway.entity.User;
import com.ontheway.enums.DeliveryStatus;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.infra.storage.R2FileUploader;
import com.ontheway.repository.DeliveryOrderRepository;
import com.ontheway.repository.DeliveryRepository;
import com.ontheway.repository.FailedAndCancelledRepository;
import com.ontheway.repository.ImageRepository;
import com.ontheway.repository.ProductRepository;
import com.ontheway.repository.RequestRepository;
import com.ontheway.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.exception.SdkException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * 배송 진행 처리. {@code POST /order} 하나가 수락부터 완료 확인까지 여섯 가지 전이를 받는다.
 *
 * 요청 DTO 에 동작을 가리키는 필드가 없어서, 무엇을 하려는지는 입력에서 추론한다.
 *
 *   requestId 있음      -> 수락
 *   cancelReason 있음   -> 취소
 *   failReason 있음     -> 배송 실패
 *   image 있음          -> 배송완료 확인요청
 *   아무것도 없음        -> 현재 상태가 정한다(픽업중 = 픽업 완료, 확인요청 = 완료 확인)
 *
 * 수락에만 requestId 를 쓰는 게 이 규칙의 핵심이다. 수락 요청이 재전송(더블클릭, 재시도)되면 상태는
 * 이미 픽업중인데, requestId 로 수락을 못 박아 두지 않으면 같은 본문이 '픽업 완료'로 읽혀서 취소할 수
 * 있는 시점을 넘겨 버린다. 이 규칙이면 재전송은 항상 409 로 끝난다.
 *
 * "있음"은 null 여부로 본다. 사유를 빈 문자열로 보낸 취소가 '입력 없음'으로 읽혀 '픽업 완료'가 되는
 * 일을 막으려는 것이고, 빈 사유는 값 검증에서 400 이 된다.
 *
 * 전이해도 되는 상태인지, 권한이 있는지, 증빙 사진이 있는지는 엔티티가 아니라 여기서 본다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    /** {@code FailedAndCancelled.reason} 컬럼 길이. */
    private static final int REASON_MAX_LENGTH = 500;
    /** {@code Image} 컬럼 길이. */
    private static final int ORIGINAL_NAME_MAX_LENGTH = 255;
    private static final int STORED_NAME_MAX_LENGTH = 100;
    private static final int EXTENSION_MAX_LENGTH = 20;

    /** 수락은 requestId 로 갈리므로 여기 없다. */
    private enum Intent {
        PICK_UP, CANCEL, FAIL, REQUEST_COMPLETION, CONFIRM
    }

    private final UserRepository userRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProductRepository productRepository;
    private final RequestRepository requestRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final FailedAndCancelledRepository failedAndCancelledRepository;
    private final ImageRepository imageRepository;
    private final R2FileUploader r2FileUploader;

    /**
     * 사진 한 장의 용량 상한. application.properties에서 읽어온다 .
     */
    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize imageMaxSize;

    @Transactional
    public ProcessResponseDto process(Long userId, ProcessRequestDto dto, MultipartFile image) {
        //기본 유효성 검사
        require(dto != null && dto.getDeliveryId() != null, ErrorCode.INVALID_INPUT);

        // 종류가 다른 입력이 섞이면(취소와 증빙 동시에 보내기 등) 무엇을 하려는 건지 알 수 없다. 
        // 수락은 다른 입력과 함께 올 수 없다(requestId가 오면 수락으로, 이 때 다른 필드값은 들어갈 수 없도록 막음).
        int inputCount = countInputs(dto, image);
        require(inputCount <= 1, ErrorCode.INVALID_INPUT);
        boolean accepting = dto.getRequestId() != null;
        require(!accepting || inputCount == 0, ErrorCode.INVALID_INPUT);

        //요청한 유저가 DB에 진짜 존재하는 지 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 동시 수락, 동시 취소 같은 요청이 찰나의 차이로 들어왔을 때 데이터가 꼬이는 동시성 문제 방지
        // 아래의 모든 처리를 경로 단위로 한 줄로 세운다(비관적 락). 
        Delivery delivery = deliveryRepository.findByIdForUpdate(dto.getDeliveryId())
                .orElseThrow(() -> new BusinessException(ErrorCode.DELIVERY_NOT_FOUND));

        // 처리 시작 시점의 시간을 전파 및 requestId를 보냈는 지 여부(수락 vs 그 외 동작)에 따라 다른 동작 수행
        LocalDateTime now = LocalDateTime.now();
        DeliveryStatus result = accepting
                ? accept(user, delivery, dto.getRequestId(), now)
                : transition(user, delivery, dto, image, now);

        return ProcessResponseDto.builder()
                .createdAt(now)
                .deliveryStatus(result)
                .build();
    }

    // --- 수락 ---

    private DeliveryStatus accept(User user, Delivery delivery, Long requestId, LocalDateTime now) {
        // 경로 작성자만 수락할 수 있도록 검증
        require(user.checkIsOwner(delivery.getAuthor().getId()), ErrorCode.FORBIDDEN);

        // 해당 요청이 실제로 내가 수락하려는 경로의 요청이 맞는 지 검증
        Request request = requestRepository.findByIdWithParties(requestId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_NOT_FOUND));
        Long deliveryId = delivery.getId();
        Long productId = request.getProduct().getId();
        require(deliveryId.equals(request.getDelivery().getId()), ErrorCode.INVALID_INPUT);

        // 이미 수락된 요청, 거절된 요청을 수락할 수 없게 방지
        require(request.getOrder() == null, ErrorCode.INVALID_ORDER_STATE);
        require(request.getRejectedAt() == null, ErrorCode.REQUEST_CLOSED);

        // 데드락 방지 규칙(항상 경로 -> 물품 순으로만 잠근다. delivery는 위 코드에서 이미 잠근 상태)
        // 경로나 물품이 도중에 삭제되었는 지 확인
        Product product = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REQUEST_CLOSED));
        require(delivery.getDeletedAt() == null && product.getDeletedAt() == null, ErrorCode.REQUEST_CLOSED);

        // 경로당 1건만 배송 가능(중복 수락 방지)
        // 물품당 1건만 배송 가능(중복 수락 방지)
        require(!deliveryOrderRepository.existsByRequest_Delivery_Id(deliveryId), ErrorCode.DELIVERY_ALREADY_MATCHED);
        require(!deliveryOrderRepository.existsByRequest_Product_Id(productId), ErrorCode.PRODUCT_ALREADY_MATCHED);

        // 배송 주문 생성 및 저장, 상태는 픽업 중으로 변경됨
        DeliveryOrder order = DeliveryOrder.accept(request);
        deliveryOrderRepository.save(order);
        DeliveryStatus status = order.getStatus();

        // 경쟁 요청들 일괄 거절
        // 수락 발생 시 같은 물품 게시글에 대해 다른 경로에 들어간 물품 요청을 자동으로 거절시킴
        requestRepository.rejectSiblingsByDelivery(deliveryId, requestId, now);
        requestRepository.rejectSiblingsByProduct(productId, requestId, now);
        return status;
    }

    // --- 수락 이후의 전이 ---

    private DeliveryStatus transition(User user, Delivery delivery, ProcessRequestDto dto,
                                      MultipartFile image, LocalDateTime now) {
        // 해당하는 배송 주문을 가져오며, 수락된 주문이 없다면 에러
        DeliveryOrder order = deliveryOrderRepository.findByDeliveryIdWithParties(delivery.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_ORDER_STATE));

        // 요청한 유저가 전달자인지, 의뢰자인지, 권한 없음인지 확인하고 그에 맞게 동작
        Request request = order.getRequest();
        boolean isDeliverer = user.checkIsOwner(request.getDeliverer().getId());
        boolean isRequester = user.checkIsOwner(request.getRequester().getId());
        require(isDeliverer || isRequester, ErrorCode.FORBIDDEN);

        switch (resolveIntent(dto, image, order.getStatus())) {
            case PICK_UP -> pickUp(order, isDeliverer, now);
            case CANCEL -> cancel(order, user, dto.getCancelReason());
            case FAIL -> fail(order, user, isDeliverer, dto.getFailReason());
            case REQUEST_COMPLETION -> requestCompletion(order, isDeliverer, image, now);
            case CONFIRM -> confirm(order, isRequester, now);
        }
        return order.getStatus();
    }

    //전송된 데이터 종류와 현재 상태로 요청의 의도를 파악하는 함수
    private static Intent resolveIntent(ProcessRequestDto dto, MultipartFile image, DeliveryStatus status) {
        if (dto.getCancelReason() != null) {
            return Intent.CANCEL;
        }
        if (dto.getFailReason() != null) {
            return Intent.FAIL;
        }
        if (hasImage(image)) {
            return Intent.REQUEST_COMPLETION;
        }
        // 입력이 없으면 상태가 동작을 정한다.
        return switch (status) {
            case PICKING_UP -> Intent.PICK_UP;
            // 사진이 없는 확인요청이다. 사진 검사에서 PROOF_IMAGE_REQUIRED 로 막힌다.
            case DELIVERING -> Intent.REQUEST_COMPLETION;
            case COMPLETION_REQUESTED -> Intent.CONFIRM;
            // 배송대기중은 예정 시각에 자동으로 넘어갈 뿐이고, 나머지는 끝난 거래다.
            default -> throw new BusinessException(ErrorCode.INVALID_ORDER_STATE);
        };
    }

    /** 전달자가 물건을 받았다. 이 뒤로는 취소할 수 없다. */
    private void pickUp(DeliveryOrder order, boolean isDeliverer, LocalDateTime now) {
        require(isDeliverer, ErrorCode.FORBIDDEN);
        requireStatus(order, DeliveryStatus.PICKING_UP);
        order.pickUp(now);
    }

    /** 취소는 당사자 양쪽이 할 수 있다. 당사자인지는 호출 전에 확인했다. */
    private void cancel(DeliveryOrder order, User user, String reason) {
        requireStatus(order, DeliveryStatus.PICKING_UP);
        String validReason = requireReason(reason);
        order.cancel();
        failedAndCancelledRepository.save(FailedAndCancelled.canceledBy(order, user, validReason));
    }

    /** 실패는 전달자만, 픽업 완료 뒤(취소 불가)부터 확인요청 전까지만 할 수 있다. */
    private void fail(DeliveryOrder order, User user, boolean isDeliverer, String reason) {
        require(isDeliverer, ErrorCode.FORBIDDEN);
        requireStatus(order, DeliveryStatus.DELIVERY_WAITING, DeliveryStatus.DELIVERING);
        String validReason = requireReason(reason);
        order.fail();
        failedAndCancelledRepository.save(FailedAndCancelled.failedBy(order, user, validReason));
    }

    private void requestCompletion(DeliveryOrder order, boolean isDeliverer, MultipartFile image, LocalDateTime now) {
        require(isDeliverer, ErrorCode.FORBIDDEN);
        requireStatus(order, DeliveryStatus.DELIVERING);
        // 증빙 사진이 없으면 확인요청으로 넘어가지 않는다.
        require(hasImage(image), ErrorCode.PROOF_IMAGE_REQUIRED);
        validateImage(image);

        // 업로드는 다른 검증이 다 끝난 뒤에 한다(중도에 막힌 요청이 R2 에 고아 파일을 남기지 않도록)
        imageRepository.save(uploadProof(order, image));
        order.requestCompletion(now);
    }

    /** 의뢰자가 받았다고 확인한다. 72시간이 지나면 스케줄러가 대신 넘긴다. */
    private void confirm(DeliveryOrder order, boolean isRequester, LocalDateTime now) {
        require(isRequester, ErrorCode.FORBIDDEN);
        requireStatus(order, DeliveryStatus.COMPLETION_REQUESTED);
        order.complete(now);
    }

    // --- 증빙 사진 ---
    // R2 클라우드 저장소에 파일 업로드하는 함수
    private Image uploadProof(DeliveryOrder order, MultipartFile image) {
        String fileUrl;
        try {
            fileUrl = r2FileUploader.upload(image);
        } catch (IOException | SdkException e) {
            // BusinessException 로그에는 원인이 안 남기에 예외를 출력
            log.error("증빙 사진 업로드 실패. orderId={}", order.getId(), e);
            throw new BusinessException(ErrorCode.IMAGE_UPLOAD_FAILED);
        }

        String originalName = originalNameOf(image);
        return Image.builder()
                .order(order)
                .fileUrl(fileUrl)
                .originalName(originalName)
                // 업로더가 UUID 로 지은 키가 URL 의 마지막 조각이다.
                .storedName(truncate(fileUrl.substring(fileUrl.lastIndexOf('/') + 1), STORED_NAME_MAX_LENGTH))
                .fileSize(image.getSize())
                .extension(extensionOf(originalName, image.getContentType()))
                .build();
    }

    //업로드 된 파일이 이미지인지, 용량 제한을 통과하는 지 확인
    private void validateImage(MultipartFile image) {
        String contentType = image.getContentType();
        boolean isImage = contentType != null && contentType.toLowerCase(Locale.ROOT).startsWith("image/");
        require(isImage && image.getSize() <= imageMaxSize.toBytes(), ErrorCode.INVALID_IMAGE);
    }

    //파일명이 이상해도 안전하게 실제 파일명을 추출하거나 임의의 값 부여
    private static String originalNameOf(MultipartFile image) {
        String name = image.getOriginalFilename();
        if (name != null) {
            // 브라우저에 따라 경로가 통째로 올 때가 있다.
            name = name.substring(Math.max(name.lastIndexOf('/'), name.lastIndexOf('\\')) + 1);
        }
        if (name == null || name.isBlank()) {
            return "proof";
        }
        return truncate(name, ORIGINAL_NAME_MAX_LENGTH);
    }

    /** 확장자 추출 함수 */
    /** 점 없이 소문자. 파일명에 확장자가 없으면 content-type(image/jpeg)의 뒷부분으로 대신한다. */
    private static String extensionOf(String originalName, String contentType) {
        String extension = "";
        int dot = originalName.lastIndexOf('.');
        if (dot >= 0) {
            extension = originalName.substring(dot + 1);
        } else if (contentType != null) {
            String mediaType = contentType.split(";")[0];
            extension = mediaType.substring(mediaType.indexOf('/') + 1);
        }
        extension = extension.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        return extension.isEmpty() ? "img" : truncate(extension, EXTENSION_MAX_LENGTH);
    }

    // --- 공통 ---

    // 취소 사유, 실패 사유, 첨부 사진이 각각 몇 개나 들어왔는 지 카운트
    private static int countInputs(ProcessRequestDto dto, MultipartFile image) {
        int count = 0;
        if (dto.getCancelReason() != null) {
            count++;
        }
        if (dto.getFailReason() != null) {
            count++;
        }
        if (hasImage(image)) {
            count++;
        }
        return count;
    }

    /** 파일을 고르지 않고 보낸 빈 파트는 사진이 아니다. */
    private static boolean hasImage(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    // 취소, 실패 시에 이유 값 유효성 검사
    private static String requireReason(String reason) {
        String stripped = reason == null ? "" : reason.strip();
        require(!stripped.isEmpty() && stripped.length() <= REASON_MAX_LENGTH, ErrorCode.INVALID_INPUT);
        return stripped;
    }

    // 현재 배송 상태가 정해진 상태 목록 내 값을 가지고 있는 지 검증
    private static void requireStatus(DeliveryOrder order, DeliveryStatus... allowed) {
        require(List.of(allowed).contains(order.getStatus()), ErrorCode.INVALID_ORDER_STATE);
    }

    private static void require(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }

    // DB 최대 컬럼을 초과하는 지 검사하는 함수
    private static String truncate(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
