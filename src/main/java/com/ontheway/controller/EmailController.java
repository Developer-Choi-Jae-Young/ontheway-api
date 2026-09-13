package com.ontheway.controller;

import com.ontheway.dto.request.EmailAuthRequestDto;
import com.ontheway.dto.request.EmailValidRequestDto;
import com.ontheway.dto.response.EmailAuthResponseDto;
import com.ontheway.dto.response.EmailValidResponseDto;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email")
@Tag(name = "이메일 인증")
public class EmailController {
    @PostMapping("/auth")
    @Operation(summary = "이메일 인증 요청")
    public ApiResponse<?> auth(@RequestBody EmailAuthRequestDto emailAuthRequestDto) {
        return ApiResponse.success(EmailAuthResponseDto.builder().build());
    }

    @PostMapping("/valid")
    @Operation(summary = "이메일 인증 확인")
    public ApiResponse<?> valid(@RequestBody EmailValidRequestDto emailValidRequestDto) {
        return ApiResponse.success(EmailValidResponseDto.builder().build());
    }
}
