package com.ontheway.dto.request;

import com.ontheway.enums.PaymentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductUpdateRequestDto implements ProductContentRequest {
    @Schema(description = "상품 ID")
    private Long productId;
    @Schema(description = "상품명")
    private String productName;
    @Schema(description = "배송지 주소")
    private String productDeliveryAddress;
    @Schema(description = "물건 수령지(productDeliveryAddress)의 위도")
    private BigDecimal productDeliveryLatitude;
    @Schema(description = "물건 수령지(productDeliveryAddress)의 경도")
    private BigDecimal productDeliveryLongitude;
    @Schema(description = "도착지 주소")
    private String endAddress;
    @Schema(description = "도착지(endAddress)의 위도")
    private BigDecimal endLatitude;
    @Schema(description = "도착지(endAddress)의 경도")
    private BigDecimal endLongitude;
    @Schema(description = "상품 정보")
    private String productInfo;
    @Schema(description = "배송비")
    private Integer deliveryFee;
    @Schema(description = "수령 예정 시간")
    private LocalDateTime receivingTime;
    @Schema(description = "희망 배송 시간")
    private LocalDateTime desiredDeliveryTime;
    @Schema(description = "결제 방식")
    private PaymentType paymentType;
}
