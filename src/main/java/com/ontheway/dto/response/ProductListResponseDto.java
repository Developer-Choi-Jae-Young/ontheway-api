package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductListResponseDto {
    @Schema(description = "물품 목록")
    private List<Product> productList;
    @Schema(description = "다음 페이지 존재 여부")
    private boolean hasNext;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        @Schema(description = "물품 게시글 ID")
        private Long productId;
        @Schema(description = "물품 일련번호")
        private Long productSerialNumber;
        @Schema(description = "물품 이름")
        private String productName;
        @Schema(description = "배송비")
        private Integer deliveryPrice;
    }
}
