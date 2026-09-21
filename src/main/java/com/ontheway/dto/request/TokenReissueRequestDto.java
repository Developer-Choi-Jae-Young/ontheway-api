package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TokenReissueRequestDto {
    @NotBlank
    @Schema(description = "리프레시 토큰")
    private String refreshToken;
}
