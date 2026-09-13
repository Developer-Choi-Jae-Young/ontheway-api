package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeliveryUpdateRequestDto {
    @Schema(description = "배송 ID")
    private Long deliveryId;
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
}
