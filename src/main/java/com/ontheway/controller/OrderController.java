package com.ontheway.controller;

import com.ontheway.dto.request.ProcessRequestDto;
import com.ontheway.dto.response.ProcessResponseDto;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/order")
@Tag(name = "배송 로직")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배송 로직 처리")
    public ApiResponse<ProcessResponseDto> process(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestPart ProcessRequestDto processRequestDto,
                                                   @RequestPart(required = false) MultipartFile image) {
        return ApiResponse.success(orderService.process(userDetails.getUserId(), processRequestDto, image));
    }
}
