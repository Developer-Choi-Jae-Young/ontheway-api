package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberRatingRequestDto {
    @Schema(description = "아이디")
    private String accountId;
}
