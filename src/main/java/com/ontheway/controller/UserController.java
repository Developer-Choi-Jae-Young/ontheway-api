package com.ontheway.controller;

import com.ontheway.dto.request.*;
import com.ontheway.dto.response.*;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/user")
@Tag(name = "회원 관리")
public class UserController {
    @PostMapping("/signup")
    @Operation(summary = "회원가입")
    public ApiResponse<?> signUp(@RequestBody MemberSaveRequestDto memberSaveRequestDto) {
        return ApiResponse.success(MemberSaveResponseDto.builder().build());
    }

    @PostMapping("/check/id")
    @Operation(summary = "아이디 중복검사")
    public ApiResponse<?> checkId(@RequestBody MemberCheckIdReqeustDto memberCheckIdReqeustDto) {
        return ApiResponse.success(MemberCheckIdResponseDto.builder().build());
    }

    @PostMapping("/find/id")
    @Operation(summary = "아이디 찾기")
    public ApiResponse<?> findId(@RequestBody MemberFindIdRequestDto memberFindIdRequestDto) {
        return ApiResponse.success(MemberFindIdResponseDto.builder().build());
    }

    @PostMapping("/find/password")
    @Operation(summary = "비밀번호 찾기")
    public ApiResponse<?> findPassword(@RequestBody MemberFindPasswordRequestDto memberFindPasswordRequestDto) {
        return ApiResponse.success(MemberFindPasswordResponseDto.builder().build());
    }

    @GetMapping("/info")
    @Operation(summary = "내 정보 조회")
    public ApiResponse<?> info() {
        return ApiResponse.success(MemberDetailResponseDto.builder().build());
    }

    @PatchMapping(name = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "내 정보 수정")
    public ApiResponse<?> updateInfo(@RequestPart MemberUpdateInfoRequestDto memberUpdateInfoRequestDto, @RequestPart(required = false) MultipartFile image) {
        return ApiResponse.success(MemberUpdateInfoResponseDto.builder().build());
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃")
    public ApiResponse<?> logout(@RequestBody MemberLogoutRequestDto memberLogoutRequestDto) {
        return ApiResponse.success(MemberLogoutResponseDto.builder().build());
    }

    @DeleteMapping("/account")
    @Operation(summary = "회원 탈퇴")
    public ApiResponse<?> deleteAccount(@RequestBody MemberDeleteAccountRequestDto memberDeleteAccountRequestDto) {
        return ApiResponse.success(MemberDeleteAccountResponseDto.builder().build());
    }

    @GetMapping("/ratings")
    @Operation(summary = "내 만족도 조회")
    public ApiResponse<?> ratings(MemberRatingRequestDto memberRatingRequestDto) {
        return ApiResponse.success(MemberRatingResponseDto.builder().build());
    }
}
