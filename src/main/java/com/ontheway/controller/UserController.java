package com.ontheway.controller;

import com.ontheway.dto.request.*;
import com.ontheway.dto.response.*;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.infra.storage.R2FileUploader;
import com.ontheway.service.AuthService;
import com.ontheway.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
@Tag(name = "회원 관리")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final R2FileUploader r2FileUploader;

    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<?> signUp(@RequestBody MemberSaveRequestDto dto) {
        userService.signup(dto);
        return ApiResponse.success(MemberSaveResponseDto.builder().createdAt(LocalDateTime.now()).build());
    }

    @PostMapping("/check/id")
    @Operation(summary = "아이디 중복검사")
    public ApiResponse<?> checkId(@RequestBody MemberCheckIdReqeustDto dto) {
        boolean isDuplicated = userService.isDuplicated(dto);
        return ApiResponse.success(MemberCheckIdResponseDto.builder().isExist(isDuplicated).build());
    }

    @PostMapping("/find/id")
    @Operation(summary = "아이디 찾기")
    public ApiResponse<?> findId(@RequestBody @Valid MemberFindIdRequestDto dto) {
        return ApiResponse.success(userService.findId(dto));
    }

    @PostMapping("/find/password")
    @Operation(summary = "비밀번호 찾기")
    public ApiResponse<?> findPassword(@RequestBody @Valid MemberFindPasswordRequestDto dto) {
        return ApiResponse.success(userService.resetPassword(dto));
    }

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<?> info(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userService.getInfo(userDetails.getUserId()));
    }

    @PatchMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "내 정보 수정")
    public ApiResponse<?> updateInfo(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @RequestPart MemberUpdateInfoRequestDto dto,
                                     @RequestPart(required = false) MultipartFile image) throws IOException {
        String imageUrl = (image != null && !image.isEmpty())
                ? r2FileUploader.upload(image)
                : null;
        return ApiResponse.success(userService.updateInfo(userDetails.getUserId(), dto, imageUrl));
    }

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급")
    public ApiResponse<MemberLoginResponseDto> reissue(@RequestBody @Valid TokenReissueRequestDto  dto) {
        return ApiResponse.success(authService.reissue(dto));
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ApiResponse<Void> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        authService.logout(userDetails.getAccountId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/account")
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<?> deleteAccount(@AuthenticationPrincipal CustomUserDetails userDetails,
                                        @RequestBody @Valid MemberDeleteAccountRequestDto dto) {
        return ApiResponse.success(userService.withdraw(userDetails.getUserId(), dto));
    }

    @GetMapping("/ratings")
    @Operation(summary = "내 후기 조회")
    public ApiResponse<?> ratings(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ApiResponse.success(userService.getRatings(userDetails.getUserId()));
    }

}
