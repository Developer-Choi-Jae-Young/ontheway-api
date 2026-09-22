package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberDeleteAccountRequestDto {
    @NotBlank
    @Schema(description = "비밀번호 확인")
    private String password;
}
