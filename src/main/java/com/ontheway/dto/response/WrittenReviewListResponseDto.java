package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WrittenReviewListResponseDto {
    @Schema(description = "후기 목록")
    private List<Review> reviewList;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Review {
        @Schema(description = "후기 ID")
        private Long reviewId;
        @Schema(description = "후기 내용")
        private String reviewContent;
        @Schema(description = "평점")
        private BigDecimal rating;
        @Schema(description = "평가 대상 이미지")
        private String targetImage;
        @Schema(description = "평가 대상 이름")
        private String targetName;
        @Schema(description = "후기 작성일")
        private LocalDateTime reviewDate;
    }
}
