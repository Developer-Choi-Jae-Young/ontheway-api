package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RequestSaveRequestDto {
    @Schema(description = "배달 ID")
    private Long deliveryId;
    @Schema(description = "물품 ID")
    private Long productId;
}
