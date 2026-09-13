package com.ontheway.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MemberSaveRequestDto {
    @Schema(description = "회원 아이디")
    private String userId;
    @Schema(description = "회원 이름")
    private String userName;
    @Schema(description = "회원 생일")
    private String birthday;
    @Schema(description = "회원 이메일")
    private String email;
    @Schema(description = "회원 비밀번호")
    private String password;
    @Schema(description = "회원 닉네임")
    private String nickName;
}
