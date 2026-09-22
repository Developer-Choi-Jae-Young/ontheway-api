package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRatingResponseDto {
    @Schema(description = "리뷰어")
    private String reviewerNickname;
    @Schema(description = "리뷰어 이미지")
    private String reviewerProfileImageUrl;
    @Schema(description = "별점")
    private BigDecimal rating;
    @Schema(description = "리뷰 내용")
    private String content;
    @Schema(description = "리뷰 작성된 시간")
    private LocalDateTime createdAt;
}
