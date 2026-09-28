package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReviewSaveRequestDto {
    @NotNull
    @Schema(description = "게시글 ID")
    private Long boardId;
    @NotNull
    @Schema(description = "평점")
    private Double rating;
    @Size(max = 1000)
    @Schema(description = "후기 내용")
    private String content;
}
