package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberFindIdRequestDto {
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "인증코드")
    private String validCode;
}
