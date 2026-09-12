package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmailValidRequestDto {
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "인증 코드")
    private String authCode;
}
