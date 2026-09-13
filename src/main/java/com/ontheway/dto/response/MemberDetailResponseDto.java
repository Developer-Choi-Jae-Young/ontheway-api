package com.ontheway.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberDetailResponseDto {
    @Schema(description = "유저 아이디")
    private String userId;
    @Schema(description = "유저 이미지")
    private String userImage;
    @Schema(description = "닉네임")
    private String nickName;
    @Schema(description = "평점")
    private Double rating;
    @Schema(description = "이메일")
    private String email;
    @Schema(description = "생일")
    private String birthday;
}
