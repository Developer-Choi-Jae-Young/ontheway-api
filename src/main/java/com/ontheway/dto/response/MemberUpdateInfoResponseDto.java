package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberUpdateInfoResponseDto {
    @Schema(description = "회원 정보 수정 시간")
    private LocalDateTime updatedAt;
    @Schema(description = "닉네임")
    private String nickName;
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "생년월일")
    private String birthday;
    @Schema(description = "회원 이미지")
    private String profileImageUrl;
}
