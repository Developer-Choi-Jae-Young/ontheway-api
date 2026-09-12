package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeliveryDetailRequestDto {
    @Schema(description = "배송 ID")
    private Long deliveryId;
}
