package com.ontheway.controller;

import com.ontheway.dto.request.ProductDetailRequestDto;
import com.ontheway.dto.request.ProductListRequestDto;
import com.ontheway.dto.request.ProductSaveRequestDto;
import com.ontheway.dto.request.ProductUpdateRequestDto;
import com.ontheway.dto.response.*;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/product")
@Tag(name = "물품 게시글")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping("/list")
    @Operation(summary = "물품 게시글 목록 조회")
    public ApiResponse<ProductListResponseDto> list(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                    ProductListRequestDto productListRequestDto) {
        return ApiResponse.success(productService.list(userDetails.getUserId(), productListRequestDto));
    }

    @PostMapping
    @Operation(summary = "물품 게시글 등록")
    public ApiResponse<ProductSaveResponseDto> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                      @RequestBody ProductSaveRequestDto productSaveRequestDto) {
        return ApiResponse.success(productService.create(userDetails.getUserId(), productSaveRequestDto));
    }

    @GetMapping
    @Operation(summary = "물품 게시글 상세 조회")
    public ApiResponse<ProductDetailResponseDto> detail(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        ProductDetailRequestDto productDetailRequestDto) {
        return ApiResponse.success(productService.detail(userDetails.getUserId(), productDetailRequestDto));
    }

    @PatchMapping
    @Operation(summary = "물품 게시글 수정")
    public ApiResponse<ProductUpdateResponseDto> update(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @RequestBody ProductUpdateRequestDto productUpdateRequestDto) {
        return ApiResponse.success(productService.update(userDetails.getUserId(), productUpdateRequestDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "물품 게시글 삭제")
    public ApiResponse<ProductDeleteResponseDto> delete(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long id) {
        return ApiResponse.success(productService.delete(userDetails.getUserId(), id));
    }
}
