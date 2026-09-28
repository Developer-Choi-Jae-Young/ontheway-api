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
@AllArgsConstructor
@NoArgsConstructor
public class LocationResponseDto {
    @Schema(description = "위도")
    private BigDecimal latitude;
    @Schema(description = "경도")
    private BigDecimal longitude;
    @Schema(description = "위치 갱신 시각")
    private LocalDateTime updatedAt;
}
