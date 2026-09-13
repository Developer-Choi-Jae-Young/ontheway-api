package com.ontheway.controller;

import com.ontheway.dto.request.ProductDetailRequestDto;
import com.ontheway.dto.request.ProductListRequestDto;
import com.ontheway.dto.request.ProductSaveRequestDto;
import com.ontheway.dto.request.ProductUpdateRequestDto;
import com.ontheway.dto.response.*;
import com.ontheway.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Tag(name = "물품 게시글")
public class ProductController {
    @GetMapping("/list")
    @Operation(summary = "물품 게시글 목록 조회")
    public ApiResponse<?> list(ProductListRequestDto productListRequestDto) {
        return ApiResponse.success(ProductListResponseDto.builder().build());
    }

    @PostMapping
    @Operation(summary = "물품 게시글 등록")
    public ApiResponse<?> create(@RequestBody ProductSaveRequestDto productSaveRequestDto) {
        return ApiResponse.success(ProductSaveResponseDto.builder().build());
    }

    @GetMapping
    @Operation(summary = "물품 게시글 상세 조회")
    public ApiResponse<?> detail(ProductDetailRequestDto productDetailRequestDto) {
        return ApiResponse.success(ProductDetailResponseDto.builder().build());
    }

    @PatchMapping
    @Operation(summary = "물품 게시글 수정")
    public ApiResponse<?> update(@RequestBody ProductUpdateRequestDto productUpdateRequestDto) {
        return ApiResponse.success(ProductUpdateResponseDto.builder().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "물품 게시글 삭제")
    public ApiResponse<?> delete(@PathVariable Long id) {
        return ApiResponse.success(ProductDeleteResponseDto.builder().build());
    }
}
