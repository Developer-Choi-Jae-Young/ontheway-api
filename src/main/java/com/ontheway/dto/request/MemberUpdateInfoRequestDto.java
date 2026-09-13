package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberUpdateInfoRequestDto {
    @Schema(description = "닉네임")
    private String nickName;
    @Schema(description = "새로운 비밀번호")
    private String newPassword;
    @Schema(description = "이메일")
    private String newEmail;
    @Schema(description = "생일")
    private String newBirthday;
}
