package com.ontheway.service;

import com.ontheway.dto.request.ProductDetailRequestDto;
import com.ontheway.dto.request.ProductListRequestDto;
import com.ontheway.dto.request.ProductSaveRequestDto;
import com.ontheway.dto.request.ProductUpdateRequestDto;
import com.ontheway.dto.response.ProductDeleteResponseDto;
import com.ontheway.dto.response.ProductDetailResponseDto;
import com.ontheway.dto.response.ProductListResponseDto;
import com.ontheway.dto.response.ProductSaveResponseDto;
import com.ontheway.dto.response.ProductUpdateResponseDto;
import com.ontheway.entity.Location;
import com.ontheway.entity.Product;
import com.ontheway.entity.User;
import com.ontheway.enums.PaymentType;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.repository.DeliveryOrderRepository;
import com.ontheway.repository.ProductRepository;
import com.ontheway.repository.ProductSerialNumber;
import com.ontheway.repository.RequestRepository;
import com.ontheway.repository.UserRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 내 물품 게시글(의뢰 물품). 목록, 등록, 상세, 수정, 삭제.
 *
 * 모든 조회와 변경은 작성자 본인 것만 다룬다. 없는 물품, 삭제된 물품, 남의 물품은 구분하지 않고 전부
 * PRODUCT_NOT_FOUND 로 답한다.
 *
 * 수정과 삭제는 수락된 거래에 쓰인 물품이면 못 한다. 수락 쪽({@code OrderService})은
 * 물품 행을 잠근 뒤 수락 여부를 확인하고 배송을 만드니, 여기서도 같은 행을 먼저 잠근 다음에 확인해야
 * 둘이 어긋나지 않는다. 잠금 없이 확인하면 방금 수락돼 아직 커밋 안 된 배송이 안 보여서, 수락된 물품이
 * 수정되는 틈이 생긴다.
 */
@Service
@RequiredArgsConstructor
public class ProductService {

    /** {@code Product} 컬럼 길이. */
    private static final int NAME_MAX_LENGTH = 100;
    private static final int INFO_MAX_LENGTH = 500;
    private static final int ADDRESS_MAX_LENGTH = 255;
    /** 좌표 컬럼이 DECIMAL(10,7) 이다. */
    private static final int COORDINATE_SCALE = 7;
    private static final int MAX_LATITUDE = 90;
    private static final int MAX_LONGITUDE = 180;

    /** 상세 응답의 시각 문자열. Jackson 이 LocalDateTime 을 내보내는 형식과 같다. */
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final RequestRepository requestRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;

    // --- 목록 ---

    @Transactional(readOnly = true)
    public ProductListResponseDto list(Long userId, ProductListRequestDto dto) {
        // 페이지 크기 값에 대한 유효성 검사
        require(dto != null && dto.getPage() >= 0 && dto.getSize() >= 1, ErrorCode.INVALID_INPUT);

        // 다음 페이지가 존재하는 지 확인(무한스크롤을 위함)
        Slice<Product> slice = productRepository.searchMyProducts(
                userId, dto.getKeyword(), PageRequest.of(dto.getPage(), dto.getSize()));

        // 일련번호 일괄 조회 및 DTO 매핑
        Map<Long, Long> serialNumbers = serialNumbersOf(slice.getContent());

        List<ProductListResponseDto.Product> products = slice.getContent().stream()
                .map(product -> ProductListResponseDto.Product.builder()
                        .productId(product.getId())
                        .productSerialNumber(serialNumbers.get(product.getId()))
                        .productName(product.getItemName())
                        .deliveryPrice(product.getDeliveryFee())
                        .build())
                .toList();

        return ProductListResponseDto.builder()
                .productList(products)
                .hasNext(slice.hasNext())
                .build();
    }

    /** 리스트 -> 해시 테이블 변환
     * 한 페이지 분량을 쿼리 한 번에 가져와 (key: 물품ID, value: 일련번호)으로 변환. 빈 페이지면 쿼리를 아예 안 보냄 */
    private Map<Long, Long> serialNumbersOf(List<Product> products) {
        if (products.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = products.stream().map(Product::getId).toList();
        return productRepository.findSerialNumbers(ids).stream()
                .collect(Collectors.toMap(ProductSerialNumber::getProductId, ProductSerialNumber::getSerialNumber));
    }

    // --- 등록 ---

    @Transactional
    public ProductSaveResponseDto create(Long userId, ProductSaveRequestDto dto) {
        require(dto != null, ErrorCode.INVALID_INPUT);
        Product.Content content = toContent(Input.builder()
                .name(dto.getProductName())
                .info(dto.getProductInfo())
                .pickupAddress(dto.getProductDeliveryAddress())
                .pickupLatitude(dto.getProductDeliveryLatitude())
                .pickupLongitude(dto.getProductDeliveryLongitude())
                .destinationAddress(dto.getEndAddress())
                .destinationLatitude(dto.getEndLatitude())
                .destinationLongitude(dto.getEndLongitude())
                .pickupTime(dto.getReceivingTime())
                .desiredArrivalTime(dto.getDesiredDeliveryTime())
                .paymentType(dto.getPaymentType())
                .deliveryFee(dto.getDeliveryFee())
                .build());
        //물품을 등록하려는 사용자가 DB에 존재하는 지 확인(탈퇴 등도 검증)
        User author = userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 허용 물품·책임 동의 여부는 프론트가 확인(백엔드에서 동의 여부를 검사하지 않음)
        // 서버는 검증하지 않고 등록 시각을 동의 시각으로 기록만 함
        Product saved = productRepository.save(Product.builder()
                .author(author)
                .content(content)
                .termsAgreedAt(LocalDateTime.now())
                .build());

        return ProductSaveResponseDto.builder()
                .productId(saved.getId())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // --- 상세 ---

    @Transactional(readOnly = true)
    public ProductDetailResponseDto detail(Long userId, ProductDetailRequestDto dto) {
        require(dto != null && dto.getProductId() != null, ErrorCode.INVALID_INPUT); //유효성 검사

        //삭제된 물품을 제외하여 조회, 작성자가 요청을 보낸 사람과 일치하는 지 검증
        Product product = productRepository.findByIdAndDeletedAtIsNull(dto.getProductId())
                .filter(found -> isAuthor(found, userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

        // DTO 변환
        User author = product.getAuthor();
        return ProductDetailResponseDto.builder()
                .productId(product.getId())
                .productName(product.getItemName())
                .userImage(author.getProfileImageUrl())
                .userName(author.getName())
                .productDeliveryAddress(product.getPickup().getAddress())
                .productDeliveryLatitude(product.getPickup().getLatitude())
                .productDeliveryLongitude(product.getPickup().getLongitude())
                .deliveryDestination(product.getDestination().getAddress())
                .endLatitude(product.getDestination().getLatitude())
                .endLongitude(product.getDestination().getLongitude())
                .productInfo(product.getItemInfo())
                .deliveryFee(product.getDeliveryFee())
                .receivingTime(TIME_FORMAT.format(product.getPickupTime()))
                .desiredDeliveryTime(TIME_FORMAT.format(product.getDesiredArrivalTime()))
                .paymentType(product.getPaymentType().name())
                .createdAt(product.getCreatedAt())
                .build();
    }

    // --- 수정 ---

    @Transactional
    public ProductUpdateResponseDto update(Long userId, ProductUpdateRequestDto dto) {
        require(dto != null && dto.getProductId() != null, ErrorCode.INVALID_INPUT);
        Product.Content content = toContent(Input.builder()
                .name(dto.getProductName())
                .info(dto.getProductInfo())
                .pickupAddress(dto.getProductDeliveryAddress())
                .pickupLatitude(dto.getProductDeliveryLatitude())
                .pickupLongitude(dto.getProductDeliveryLongitude())
                .destinationAddress(dto.getEndAddress())
                .destinationLatitude(dto.getEndLatitude())
                .destinationLongitude(dto.getEndLongitude())
                .pickupTime(dto.getReceivingTime())
                .desiredArrivalTime(dto.getDesiredDeliveryTime())
                .paymentType(dto.getPaymentType())
                .deliveryFee(dto.getDeliveryFee())
                .build());

        Product product = lockOwned(userId, dto.getProductId());
        requireNotAccepted(product);
        // 변경 내용이 없는 수정은 받지 않음
        require(!isSame(product, content), ErrorCode.PRODUCT_NOT_CHANGED);

        product.update(content);
        // 응답 DTO에 JPA Auditing으로 자동 채워진 최신 updatedAt을 담기 위해 즉시 flush
        productRepository.saveAndFlush(product);
        return ProductUpdateResponseDto.builder().updatedAt(product.getUpdatedAt()).build();
    }

    // --- 삭제 ---

    @Transactional
    public ProductDeleteResponseDto delete(Long userId, Long productId) {
        require(productId != null, ErrorCode.INVALID_INPUT);
        Product product = lockOwned(userId, productId);
        requireNotAccepted(product);

        LocalDateTime now = LocalDateTime.now();
        product.delete(now);
        // 타 조회 쿼리가 rejectedAt만 참조하므로, 물품 삭제 시 연관된 요청도 함께 거절 처리
        // 벌크 UPDATE 실행 시 1차 캐시가 초기화(clear)되므로
        // 이 로직은 항상 트랜잭션 최하단에 위치해야 하며 이후 기존 엔티티를 참조해선 안 됨
        requestRepository.rejectByProduct(productId, now);

        return ProductDeleteResponseDto.builder().updatedAt(now).build();
    }

    // --- 수정/삭제 공통 ---

    /** 행을 잠그고 본인의 살아있는 물품인지 본다. 잠금이 먼저여야 뒤의 수락 여부 확인이 의미가 있다. */
    private Product lockOwned(Long userId, Long productId) {
        return productRepository.findByIdForUpdate(productId)
                .filter(found -> found.getDeletedAt() == null && isAuthor(found, userId))
                .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    /**
     * 한 번이라도 수락된 물품은 그 거래가 취소나 실패로 끝나도 계속 못 고친다.
     * ({@code DeliveryOrderRepository#existsByRequest_Product_Id}).
     */
    private void requireNotAccepted(Product product) {
        require(!deliveryOrderRepository.existsByRequest_Product_Id(product.getId()), ErrorCode.PRODUCT_LOCKED);
    }

    private static boolean isAuthor(Product product, Long userId) {
        return product.getAuthor().getId().equals(userId);
    }

    // --- 입력 검증 ---

    /* 매개변수 순서 오류 방지
     * 등록과 수정이 받는 값. 같은 타입이 여러 개라 실수로 순서를 바꿔 써도 컴파일이 통과해서 에러를 잡지 못하기 때문 */
    @Builder
    private record Input(
            String name, String info,
            String pickupAddress, BigDecimal pickupLatitude, BigDecimal pickupLongitude,
            String destinationAddress, BigDecimal destinationLatitude, BigDecimal destinationLongitude,
            LocalDateTime pickupTime, LocalDateTime desiredArrivalTime,
            PaymentType paymentType, Integer deliveryFee) {
    }

    private static Product.Content toContent(Input input) {
        String name = requireText(input.name(), NAME_MAX_LENGTH);
        String info = blankToNull(input.info());
        require(info == null || info.length() <= INFO_MAX_LENGTH, ErrorCode.INVALID_INPUT);

        String pickupAddress = requireText(input.pickupAddress(), ADDRESS_MAX_LENGTH);
        String destinationAddress = requireText(input.destinationAddress(), ADDRESS_MAX_LENGTH);
        Location pickup = location(pickupAddress, input.pickupLatitude(), input.pickupLongitude());
        Location destination = location(destinationAddress, input.destinationLatitude(), input.destinationLongitude());

        require(input.pickupTime() != null && input.desiredArrivalTime() != null
                && input.paymentType() != null && input.deliveryFee() != null, ErrorCode.INVALID_INPUT);
        // 배송비는 0 이상, 희망 도착 시간은 수령 시간보다 뒤여야 한다.
        require(input.deliveryFee() >= 0, ErrorCode.INVALID_INPUT);
        require(input.desiredArrivalTime().isAfter(input.pickupTime()), ErrorCode.INVALID_INPUT);

        return Product.Content.builder()
                .itemName(name)
                .itemInfo(info)
                .pickup(pickup)
                .destination(destination)
                .pickupTime(input.pickupTime())
                .desiredArrivalTime(input.desiredArrivalTime())
                .paymentType(input.paymentType())
                .deliveryFee(input.deliveryFee())
                .build();
    }

    private static Location location(String address, BigDecimal latitude, BigDecimal longitude) {
        require(latitude != null && longitude != null, ErrorCode.INVALID_INPUT);
        // 저장될 값(소수 7자리)으로 먼저 맞춘 뒤 범위를 본다. 수정에서 '변경 없음' 비교도 같은 자릿수로 해야 한다.
        BigDecimal scaledLatitude = latitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
        BigDecimal scaledLongitude = longitude.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP);
        require(within(scaledLatitude, MAX_LATITUDE) && within(scaledLongitude, MAX_LONGITUDE), ErrorCode.INVALID_INPUT);
        return Location.builder().address(address).latitude(scaledLatitude).longitude(scaledLongitude).build();
    }

    private static boolean within(BigDecimal value, int limit) {
        return value.abs().compareTo(BigDecimal.valueOf(limit)) <= 0;
    }

    /** 앞뒤 공백을 뗀 값을 돌려준다. 비어 있거나 상한을 넘으면 400. */
    private static String requireText(String value, int maxLength) {
        String stripped = value == null ? "" : value.strip();
        require(!stripped.isEmpty() && stripped.length() <= maxLength, ErrorCode.INVALID_INPUT);
        return stripped;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    // --- 변경 여부 ---

    /**
     * 저장된 값과 새 값이 모두 같은지. {@code Location} 에 equals 가 없어서 필드별로 비교하고, 좌표는
     * 자릿수가 달라도(37.5665 와 37.5665000) 같은 값으로 본다.
     */
    private static boolean isSame(Product product, Product.Content content) {
        return Objects.equals(product.getItemName(), content.itemName())
                && Objects.equals(product.getItemInfo(), content.itemInfo())
                && isSame(product.getPickup(), content.pickup())
                && isSame(product.getDestination(), content.destination())
                && Objects.equals(product.getPickupTime(), content.pickupTime())
                && Objects.equals(product.getDesiredArrivalTime(), content.desiredArrivalTime())
                && product.getPaymentType() == content.paymentType()
                && Objects.equals(product.getDeliveryFee(), content.deliveryFee());
    }

    private static boolean isSame(Location stored, Location next) {
        return Objects.equals(stored.getAddress(), next.getAddress())
                && stored.getLatitude().compareTo(next.getLatitude()) == 0
                && stored.getLongitude().compareTo(next.getLongitude()) == 0;
    }

    private static void require(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }
}
