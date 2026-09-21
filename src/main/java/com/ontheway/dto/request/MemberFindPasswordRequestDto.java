package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberFindPasswordRequestDto {
    @Schema(description = "아이디")
    @NotBlank
    private String accountId;
    @Schema(description = "이메일")
    @NotBlank
    private String email;
}
