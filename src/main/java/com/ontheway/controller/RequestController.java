package com.ontheway.controller;

import com.ontheway.dto.request.RequestSaveRequestDto;
import com.ontheway.dto.response.RequestDeliveryListResponseDto;
import com.ontheway.dto.response.RequestSaveResponseDto;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/request")
@Tag(name = "물품 의뢰 요청")
@RequiredArgsConstructor
public class RequestController {
    private final RequestService requestService;

    @PostMapping
    @Operation(summary = "물품 의뢰 요청하기")
    public ApiResponse<?> register(@AuthenticationPrincipal CustomUserDetails userDetails,
                                   @RequestBody RequestSaveRequestDto dto) {
        return ApiResponse.success(requestService.register(userDetails.getUserId(), dto));
    }

    @GetMapping("/list/{deliveryId}")
    @Operation(summary = "이동 경로 게시글 상세 > 의뢰 요청 목록 조회")
    public ApiResponse<?> deliveryRequestList(@AuthenticationPrincipal CustomUserDetails userDetails,
                                              @PathVariable long deliveryId) {
        return ApiResponse.success(requestService.getDeliveryRequestList(deliveryId, userDetails.getUserId()));
    }
}
