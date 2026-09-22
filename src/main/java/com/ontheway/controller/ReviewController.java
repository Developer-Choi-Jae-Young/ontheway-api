package com.ontheway.controller;

import com.ontheway.dto.request.ReviewListRequestDto;
import com.ontheway.dto.request.ReviewSaveRequestDto;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/review")
@Tag(name = "후기")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "후기 작성하기")
    public ApiResponse<?> create(@AuthenticationPrincipal CustomUserDetails userDetails,
                                 @RequestBody ReviewSaveRequestDto dto) {
        return ApiResponse.success(reviewService.create(userDetails.getUserId(), dto));
    }

    @GetMapping("/me/list")
    @Operation(summary = "내가 받은 후기 목록 조회")
    public ApiResponse<?> list(@AuthenticationPrincipal CustomUserDetails userDetails,
                               ReviewListRequestDto dto) {
        Pageable pageable = PageRequest.of(dto.getPage(), dto.getSize());
        return ApiResponse.success(reviewService.getMyReceivedReviews(userDetails.getUserId(), pageable));
    }
}
