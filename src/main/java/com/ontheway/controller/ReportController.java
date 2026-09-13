package com.ontheway.controller;

import com.ontheway.dto.request.ReportBoardRequestDto;
import com.ontheway.dto.request.ReportUserRequestDto;
import com.ontheway.dto.response.ReportBoardResponseDto;
import com.ontheway.dto.response.ReportUserResponseDto;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@Tag(name = "신고")
public class ReportController {
    @PostMapping("/user")
    @Operation(summary = "유저 신고하기")
    public ApiResponse<?> user(@RequestBody ReportUserRequestDto reportUserRequestDto) {
        return ApiResponse.success(ReportUserResponseDto.builder().build());
    }

    @PostMapping("/board")
    @Operation(summary = "게시글 신고하기")
    public ApiResponse<?> board(@RequestBody ReportBoardRequestDto reportBoardRequestDto) {
        return ApiResponse.success(ReportBoardResponseDto.builder().build());
    }
}
