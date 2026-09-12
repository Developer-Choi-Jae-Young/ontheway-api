package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductDetailResponseDto {
    @Schema(description = "물품 ID")
    private Long productId;
    @Schema(description = "사용자 이미지")
    private String userImage;
    @Schema(description = "사용자 이름")
    private String userName;
    @Schema(description = "물품 수령지(주소)")
    private String productDeliveryAddress;
    @Schema(description = "배송 목적지")
    private String deliveryDestination;
    @Schema(description = "물품 정보")
    private String productInfo;
    @Schema(description = "배송료")
    private Integer deliveryFee;
    @Schema(description = "물건 수령 시간")
    private String receivingTime;
    @Schema(description = "희망배도착시간")
    private String desiredDeliveryTime;
    @Schema(description = "결제 방식")
    private String paymentType;
    @Schema(description = "게시 등록일")
    private LocalDateTime createdAt;
}
