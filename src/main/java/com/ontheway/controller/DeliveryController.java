package com.ontheway.controller;

import com.ontheway.dto.request.*;
import com.ontheway.dto.response.*;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery")
@Tag(name = "이동 경로 게시글")
public class DeliveryController {
    @GetMapping("/list")
    @Operation(summary = "이동 경로 게시글 목록 조회")
    public ApiResponse<DeliveryListResponseDto> list(DeliveryListRequestDto deliveryListRequestDto) {
        return ApiResponse.success(DeliveryListResponseDto.builder().build());
    }

    @GetMapping
    @Operation(summary = "이동 경로 게시글 상세 조회")
    public ApiResponse<?> detail(DeliveryDetailRequestDto deliveryDetailRequestDto) {
        return ApiResponse.success(DeliveryDetailResponseDto.builder().build());
    }

    @PostMapping
    @Operation(summary = "이동 경로 게시글 등록")
    public ApiResponse<?> create(@RequestBody DeliverySaveRequestDto deliverySaveRequestDto) {
        return ApiResponse.success(DeliverySaveResponseDto.builder().build());
    }

    @PatchMapping
    @Operation(summary = "이동 경로 게시글 수정")
    public ApiResponse<?> update(@RequestBody DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        return ApiResponse.success(DeliveryUpdateResponseDto.builder().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "이동 경로 게시글 삭제")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return ApiResponse.success(DeliveryDeleteResponseDto.builder().build());
    }

    @GetMapping("/me/list")
    @Operation(summary = "내 게시글 이동 경로 목록 조회")
    public ApiResponse<?> myList(MyBoardDeliveryListRequestDto myBoardDeliveryListRequestDto) {
        return ApiResponse.success(MyBoardDeliveryListResponseDto.builder().build());
    }
}
