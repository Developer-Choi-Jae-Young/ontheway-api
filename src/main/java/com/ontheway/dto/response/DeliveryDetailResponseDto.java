package com.ontheway.dto.response;

import com.ontheway.enums.DeliveryStatus;
import com.ontheway.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryDetailResponseDto {
    @Schema(description = "배송 ID")
    private Long deliveryId;
    @Schema(description = "현재 배송 상태")
    private DeliveryStatus currentDeliveryStatus;
    @Schema(description = "출발지 주소")
    private String startAddress;
    @Schema(description = "도착지 주소")
    private String endAddress;
    @Schema(description = "배송 일시")
    private LocalDateTime deliveryDate;
    @Schema(description = "추가 정보")
    private String addInfo;
    @Schema(description = "희망금액")
    private Integer hopePrice;
    @Schema(description = "배송 예정 시간")
    private LocalDateTime estimatedDeliveryTime;
    @Schema(description = "생성 일시")
    private LocalDateTime createdAt;
    @Schema(description = "배송 상태 변경 이력 목록")
    private List<DeliveryStatusHistory> deliveryStatusHistory;
    @Schema(description = "사용자 프로필 이미지 URL")
    private String userImage;
    @Schema(description = "사용자 이름")
    private String userName;
    @Schema(description = "요청자 정보")
    private RequesterInfo requesterInfo;
    @Schema(description = "배송 실패 정보")
    private DeliveryFail deliveryFail;
    @Schema(description = "배송 취소 정보")
    private DeliveryCancel deliveryCancel;
    @Schema(description = "배송완료확인요청 정보")
    private DeliverySuccessCheck deliverySuccessCheck;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryStatusHistory {
        @Schema(description = "배송 상태")
        private DeliveryStatus deliveryStatus;
        @Schema(description = "상태 변경 일시")
        private LocalDateTime deliveryDate;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RequesterInfo {
        @Schema(description = "사용자 프로필 이미지 URL")
        private String userImage;
        @Schema(description = "사용자 이름")
        private String userName;
        @Schema(description = "물품 수령지(주소)")
        private String productDeliveryAddress;
        @Schema(description = "배송 목적지")
        private String deliveryDestination;
        @Schema(description = "물품 정보")
        private String productInfo;
        @Schema(description = "배송비")
        private Integer deliveryFee;
        @Schema(description = "물건 수령 시간")
        private LocalDateTime receivingTime;
        @Schema(description = "희망 배송 도착 시간")
        private LocalDateTime desiredDeliveryTime;
        @Schema(description = "결제 방식")
        private PaymentType paymentType;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryFail {
        @Schema(description = "배송 실패 사유")
        private String failReason;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliveryCancel {
        @Schema(description = "배송 취소 사유")
        private String cancelReason;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DeliverySuccessCheck {
        @Schema(description = "배송완료 확인요청 이미지 URL")
        private String deliverySuccessCheckImage;
    }
}
