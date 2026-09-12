package com.ontheway.controller;

import com.ontheway.dto.request.RequestSaveRequestDto;
import com.ontheway.dto.response.RequestDeliveryListResponseDto;
import com.ontheway.dto.response.RequestSaveResponseDto;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/request")
@Tag(name = "물품 의뢰 요청")
public class RequestController {
    @PostMapping
    @Operation(summary = "물품 의뢰 요청하기")
    public ApiResponse<?> create(@RequestBody RequestSaveRequestDto requestRequestDto) {
        return ApiResponse.success(RequestSaveResponseDto.builder().build());
    }

    @GetMapping("/list/{deliveryId}")
    @Operation(summary = "이동 경로 게시글 상세 > 의뢰 요청 목록 조회")
    public ApiResponse<?> deliveryRequestList(@PathVariable long deliveryId) {
        return ApiResponse.success(RequestDeliveryListResponseDto.builder().build());
    }
}
