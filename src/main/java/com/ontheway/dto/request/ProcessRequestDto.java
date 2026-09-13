package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ProcessRequestDto {
    @Schema(description = "배송 ID")
    private Long deliveryId;
    @Schema(description = "요청 ID")
    private Long requestId;
    @Schema(description = "배송 실패 사유")
    private String failReason;
    @Schema(description = "배송 취소 사유")
    private String cancelReason;
}
