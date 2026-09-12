package com.ontheway.controller;

import com.ontheway.dto.request.HistoryListRequestDto;
import com.ontheway.dto.response.HistoryListResponseDto;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/history")
public class HistoryController {
    @GetMapping("/list")
    @Operation(summary = "이용 내역 전체 목록 조회")
    public ApiResponse<?> list(HistoryListRequestDto historyListRequestDto) {
        return ApiResponse.success(HistoryListResponseDto.builder().build());
    }

    @GetMapping("/delivery/list")
    @Operation(summary = "이용 내역 이동 경로 목록 조회")
    public ApiResponse<?> deliveryList(HistoryListRequestDto historyListRequestDto) {
        return ApiResponse.success(HistoryListResponseDto.builder().build());
    }

    @GetMapping("/request/list")
    @Operation(summary = "이용 내역 의뢰 요청 목록 조회")
    public ApiResponse<?> requestList(HistoryListRequestDto historyListRequestDto) {
        return ApiResponse.success(HistoryListResponseDto.builder().build());
    }

    @GetMapping("/cancel/list")
    @Operation(summary = "취소/실패 목록 조회")
    public ApiResponse<?> cancelList(HistoryListRequestDto historyListRequestDto) {
        return ApiResponse.success(HistoryListResponseDto.builder().build());
    }
}
