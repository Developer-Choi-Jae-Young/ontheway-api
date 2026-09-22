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
    @Schema(description = "평균 점수")
    private BigDecimal averageRating;
    @Schema(description = "총 갯수")
    private long reviewCount;
}
