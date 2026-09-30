package com.ontheway.service;

import com.ontheway.dto.request.HistoryListRequestDto;
import com.ontheway.dto.response.HistoryListResponseDto;
import com.ontheway.entity.DeliveryOrder;
import com.ontheway.entity.FailedAndCancelled;
import com.ontheway.entity.Request;
import com.ontheway.entity.User;
import com.ontheway.enums.BoardType;
import com.ontheway.enums.DeliveryStatus;
import com.ontheway.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HistoryService {
    private final UserRepository userRepository;
    private final FailedAndCancelledRepository failedAndCancelledRepository;
    private final DeliveryOrderRepository deliveryOrderRepository;
    private final RequestRepository requestRepository;

    public HistoryListResponseDto list(Long userId, HistoryListRequestDto historyListRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<DeliveryOrder> deliveryOrders =
                deliveryOrderRepository.findByRequest_Delivery_AuthorAndStatusIn(user,
                        List.of(DeliveryStatus.DELIVERY_WAITING, DeliveryStatus.PICKING_UP, DeliveryStatus.DELIVERING, DeliveryStatus.COMPLETION_REQUESTED, DeliveryStatus.COMPLETED),
                        PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> historyList = new ArrayList<>(deliveryToHistoryList(deliveryOrders));

        Page<Request> requests = requestRepository.findByProduct_Author(user, PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> requestHistoryList = requestToHistoryList(requests).stream()
                .filter(item -> item.getDeliveryStatus() != DeliveryStatus.CANCELED && item.getDeliveryStatus() != DeliveryStatus.FAILED)
                .toList();

        Page<FailedAndCancelled> failedAndCancelled = failedAndCancelledRepository.findByOrder_Request_Product_AuthorAndOrder_StatusIn(user,
                List.of(DeliveryStatus.FAILED, DeliveryStatus.CANCELED),
                PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> failAndCancelHistoryList = cancelToHistoryList(failedAndCancelled);

        historyList.addAll(requestHistoryList);
        historyList.addAll(failAndCancelHistoryList);
        return HistoryListResponseDto.builder().historyList(historyList).build();
    }

    public HistoryListResponseDto deliveryList(Long userId, HistoryListRequestDto historyListRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<DeliveryOrder> deliveryOrders =
                deliveryOrderRepository.findByRequest_Delivery_AuthorAndStatusIn(user,
                        List.of(DeliveryStatus.DELIVERY_WAITING, DeliveryStatus.PICKING_UP, DeliveryStatus.DELIVERING, DeliveryStatus.COMPLETION_REQUESTED, DeliveryStatus.COMPLETED),
                        PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> historyList = deliveryToHistoryList(deliveryOrders);
        return HistoryListResponseDto.builder().historyList(historyList).build();
    }

    public HistoryListResponseDto requestList(Long userId, HistoryListRequestDto historyListRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<Request> requests = requestRepository.findByProduct_Author(user, PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> historyList = requestToHistoryList(requests);
        return HistoryListResponseDto.builder().historyList(historyList).build();
    }

    public HistoryListResponseDto cancelList(Long userId, HistoryListRequestDto historyListRequestDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));

        Page<FailedAndCancelled> failedAndCancelledProduct = failedAndCancelledRepository.findByOrder_Request_Product_AuthorAndOrder_StatusIn(user,
                List.of(DeliveryStatus.FAILED, DeliveryStatus.CANCELED),
                PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        Page<FailedAndCancelled> failedAndCancelledDelivery = failedAndCancelledRepository.findByOrder_Request_Delivery_AuthorAndOrder_StatusIn(user,
                List.of(DeliveryStatus.FAILED, DeliveryStatus.CANCELED),
                PageRequest.of(historyListRequestDto.getPage(), historyListRequestDto.getSize()));

        List<HistoryListResponseDto.HistoryList> historyListProduct = cancelToHistoryList(failedAndCancelledProduct);
        List<HistoryListResponseDto.HistoryList> historyListDelivery = cancelToHistoryList(failedAndCancelledDelivery);
        List<HistoryListResponseDto.HistoryList> historyList = new ArrayList<>();
        historyList.addAll(historyListProduct);
        historyList.addAll(historyListDelivery);

        return HistoryListResponseDto.builder().historyList(historyList).build();
    }

    private List<HistoryListResponseDto.HistoryList> deliveryToHistoryList(Page<DeliveryOrder> deliveryOrders) {
        return deliveryOrders.map(deliveryOrder ->
                HistoryListResponseDto.HistoryList.builder()
                        .deliveryId(deliveryOrder.getDelivery().getId())
                        .deliveryStatus(deliveryOrder.getStatus())
                        .boardType(BoardType.DELIVERY)
                        .startAddress(deliveryOrder.getDelivery().getDeparture().getAddress())
                        .endAddress(deliveryOrder.getDelivery().getDestination().getAddress())
                        .deliveryDate(deliveryOrder.getDelivery().getDeliveryDate().atTime(deliveryOrder.getDelivery().getPlannedStartTime()))
                        .deliveryFee(deliveryOrder.getDeliveryFee())
                        .build()
        ).getContent();
    }

    private List<HistoryListResponseDto.HistoryList> requestToHistoryList(Page<Request> requests) {
        return requests.map(request ->
                HistoryListResponseDto.HistoryList.builder()
                        .deliveryId(request.getDelivery().getId())
                        .deliveryStatus(request.getStatus())
                        .boardType(BoardType.REQUEST)
                        .startAddress(request.getDelivery().getDeparture().getAddress())
                        .endAddress(request.getDelivery().getDestination().getAddress())
                        .deliveryDate(request.getDelivery().getDeliveryDate().atTime(request.getDelivery().getPlannedStartTime()))
                        .deliveryFee(request.getProduct().getDeliveryFee())
                        .build()
        ).getContent();
    }

    private List<HistoryListResponseDto.HistoryList> cancelToHistoryList(Page<FailedAndCancelled> failedAndCancelleds) {
        return failedAndCancelleds.map(failedAndCancelled ->
                HistoryListResponseDto.HistoryList.builder()
                        .deliveryId(failedAndCancelled.getDelivery().getId())
                        .deliveryStatus(failedAndCancelled.getOrder().getStatus())
                        .boardType(BoardType.FAILED_AND_CANCELLED)
                        .startAddress(failedAndCancelled.getDelivery().getDeparture().getAddress())
                        .endAddress(failedAndCancelled.getDelivery().getDestination().getAddress())
                        .deliveryDate(failedAndCancelled.getDelivery().getDeliveryDate().atTime(failedAndCancelled.getDelivery().getPlannedStartTime()))
                        .deliveryFee(failedAndCancelled.getDelivery().getDesiredPrice())
                        .build()
        ).getContent();
    }
}
