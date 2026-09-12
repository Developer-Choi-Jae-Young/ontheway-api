package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProductDetailRequestDto {
    @Schema(description = "상품 ID")
    private Long productId;
}
