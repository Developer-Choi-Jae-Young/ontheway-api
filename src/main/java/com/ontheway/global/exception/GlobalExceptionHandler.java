package com.ontheway.global.exception;

import com.ontheway.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(BusinessException e) {
        ErrorCode code = e.getErrorCode();
        log.warn("BusinessException: {}", code.name());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getStatus().value(), e.getMessage()));
    }

    // 업로드 용량 초과(spring.servlet.multipart.max-file-size / max-request-size).
    // 컨트롤러에 닿기 전에 멀티파트 파싱에서 터지므로 서비스가 아니라 여기서 받는다
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handleMaxUploadSize(MaxUploadSizeExceededException e) {
        ErrorCode code = ErrorCode.INVALID_IMAGE;
        log.warn("MaxUploadSizeExceededException: {}", e.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getStatus().value(), code.getMessage()));
    }

    // 그 외 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.internalServerError()
                .body(ApiResponse.fail(500, ErrorCode.INTERNAL_ERROR.getMessage()));
    }
}
