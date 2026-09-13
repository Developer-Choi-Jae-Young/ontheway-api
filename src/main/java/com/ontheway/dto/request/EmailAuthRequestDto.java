package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EmailAuthRequestDto {
    @Schema(description = "이메일")
    private String email;
}
