package com.ontheway.dto.request;

import com.ontheway.enums.ReportCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Set;

@Data
public class ReportUserRequestDto {
    @Schema(description = "신고할 유저 ID")
    private Long userId;
    @Schema(description = "신고 유형 (최대 3개)")
    private Set<ReportCategory> categories;
    @Schema(description = "신고 내용")
    private String content;
}
