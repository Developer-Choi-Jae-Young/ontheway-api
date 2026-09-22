package com.ontheway.controller;

import com.ontheway.dto.request.ReportBoardRequestDto;
import com.ontheway.dto.request.ReportUserRequestDto;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/report")
@Tag(name = "신고")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @PostMapping("/user")
    @Operation(summary = "유저 신고하기")
    public ApiResponse<?> user(@AuthenticationPrincipal CustomUserDetails userDetails,
                               @RequestBody ReportUserRequestDto dto) {
        return ApiResponse.success(reportService.reportUser(userDetails.getUserId(), dto));
    }

    @PostMapping("/board")
    @Operation(summary = "게시글 신고하기")
    public ApiResponse<?> board(@AuthenticationPrincipal CustomUserDetails userDetails,
                                @RequestBody ReportBoardRequestDto dto) {
        return ApiResponse.success(reportService.reportBoard(userDetails.getUserId(), dto));
    }
}
