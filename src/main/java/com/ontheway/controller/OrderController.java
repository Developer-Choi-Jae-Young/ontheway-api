package com.ontheway.controller;

import com.ontheway.dto.request.ProcessRequestDto;
import com.ontheway.dto.response.ProcessResponseDto;
import com.ontheway.global.response.ApiResponse;
import com.ontheway.global.security.CustomUserDetails;
import com.ontheway.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
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

    // encoding 을 안 적으면 Swagger UI 가 JSON 파트를 content-type 없이 보내고, 서버는 그걸 octet-stream 으로 읽어 실패한다.
    // Spring 의 @RequestBody 와 이름이 겹쳐서 swagger 쪽은 전체 경로로 쓴다
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "배송 로직 처리",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            encoding = @Encoding(name = "processRequestDto",
                                    contentType = MediaType.APPLICATION_JSON_VALUE))))
    public ApiResponse<ProcessResponseDto> process(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                   @RequestPart ProcessRequestDto processRequestDto,
                                                   @RequestPart(required = false) MultipartFile image) {
        return ApiResponse.success(orderService.process(userDetails.getUserId(), processRequestDto, image));
    }
}
