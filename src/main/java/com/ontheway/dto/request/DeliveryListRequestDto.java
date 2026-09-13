package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class DeliveryListRequestDto {
    @Schema(description = "만족도")
    private Double rating;
    @Schema(description = "배송출발지")
    private String startAddress;
    @Schema(description = "배송목적지")
    private String endAddress;
    @Schema(description = "희망금액")
    private Integer hopePrice;
    @Schema(description = "페이지 번호")
    private int page;
    @Schema(description = "페이지당 항목수")
    private int size;
}
