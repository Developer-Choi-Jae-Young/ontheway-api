package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReviewSaveRequestDto {
    @Schema(description = "게시글 ID")
    private Long boardId;
    @Schema(description = "평점")
    private Double rating;
    @Schema(description = "후기 내용")
    private String content;
}
