package com.ontheway.global.security.jwt;

import com.ontheway.global.exception.ErrorCode;
import com.ontheway.global.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtExceptionHandler implements AuthenticationEntryPoint, AccessDeniedHandler {
    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        // 인증되지 않은 사용자가 보호된 API에 접근했을 때
        String exceptionType = (String) request.getAttribute("exception");
        ErrorCode errorCode;

        if ("TOKEN_EXPIRED".equals(exceptionType)) {
            errorCode = ErrorCode.TOKEN_EXPIRED;
        } else if ("INVALID_TOKEN".equals(exceptionType)) {
            errorCode = ErrorCode.INVALID_TOKEN;
        } else {
            errorCode = ErrorCode.UNAUTHORIZED;
        }

        sendErrorResponse(response, errorCode);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        // 인증은 됐지만 권한이 없는 경우
        sendErrorResponse(response, ErrorCode.FORBIDDEN);
    }

    private void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType("application/json;charset=UTF-8");

        response.getWriter().write(
                objectMapper.writeValueAsString(
                        ApiResponse.fail(
                                errorCode.getStatus().value(),
                                errorCode.getMessage()
                        )
                )
        );
    }
}
