package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LocationUpdateRequestDto {
    @Schema(description = "배송 ID")
    private Long deliveryId;
    @Schema(description = "위도")
    private BigDecimal latitude;
    @Schema(description = "경도")
    private BigDecimal longitude;
}
