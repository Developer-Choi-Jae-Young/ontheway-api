package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportUserRequestDto {
    @Schema(description = "신고할 유저 ID")
    private Long userId;
    @Schema(description = "신고 사유")
    private String reason;
}
