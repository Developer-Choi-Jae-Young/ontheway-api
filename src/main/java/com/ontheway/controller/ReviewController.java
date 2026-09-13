package com.ontheway.controller;

import com.ontheway.dto.request.ReviewListRequestDto;
import com.ontheway.dto.request.ReviewSaveRequestDto;
import com.ontheway.dto.response.ReviewListResponseDto;
import com.ontheway.dto.response.ReviewSaveResponseDto;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
@Tag(name = "후기")
public class ReviewController {
    @PostMapping
    @Operation(summary = "후기 작성하기")
    public ApiResponse<?> create(@RequestBody ReviewSaveRequestDto reviewSaveRequestDto) {
        return ApiResponse.success(ReviewSaveResponseDto.builder().build());
    }

    @GetMapping("/me/list")
    @Operation(summary = "내 후기 목록 조회")
    public ApiResponse<?> list(ReviewListRequestDto reviewListRequestDto) {
        return ApiResponse.success(ReviewListResponseDto.builder().build());
    }
}
