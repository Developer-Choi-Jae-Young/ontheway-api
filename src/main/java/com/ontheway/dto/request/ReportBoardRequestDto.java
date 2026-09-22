package com.ontheway.dto.request;

import com.ontheway.enums.ReportCategory;
import com.ontheway.enums.ReportEntityType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
public class ReportBoardRequestDto {
    @Schema(description = "신고할 게시글 ID")
    private Long boardId;
    @Schema(description = "게시글 종류 (PRODUCT 또는 DELIVERY)")
    private ReportEntityType boardType;
    @Schema(description = "신고 유형 (최대 3개)")
    private Set<ReportCategory> categories;
    @Schema(description = "신고 내용")
    private String content;
}
