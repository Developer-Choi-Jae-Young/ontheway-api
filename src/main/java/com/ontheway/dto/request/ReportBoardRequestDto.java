package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportBoardRequestDto {
    @Schema(description = "신고할 게시글 ID")
    private Long boardId;
    @Schema(description = "신고 사유")
    private String reason;
}
