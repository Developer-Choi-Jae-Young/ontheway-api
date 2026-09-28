package com.ontheway.service;

import com.ontheway.dto.request.TokenReissueRequestDto;
import com.ontheway.dto.response.MemberLoginResponseDto;
import com.ontheway.dto.response.TokenRenewResponseDto;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.global.security.jwt.JwtTokenProvider;
import com.ontheway.infra.cache.LoginAttemptLimiter;
import com.ontheway.infra.cache.RefreshTokenStore;
import com.ontheway.infra.cache.RotationResult;
import com.ontheway.infra.cache.TokenValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;
    private final LoginAttemptLimiter loginAttemptLimiter;

    public MemberLoginResponseDto reissue(TokenReissueRequestDto dto) {
        String refreshToken = dto.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            if (jwtTokenProvider.isExpired(refreshToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        if (!"refresh".equals(jwtTokenProvider.getCategory(refreshToken))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String accountId = jwtTokenProvider.getAccountId(refreshToken);
        String candidateNewRefreshToken = jwtTokenProvider.createRefreshToken(accountId);
        RotationResult result = refreshTokenStore.rotate(accountId, refreshToken, candidateNewRefreshToken);

        if (result.status() == TokenValidationResult.REUSED) {
            log.warn("[SECURITY] RefreshToken 재사용 감지 - accountId: {}", accountId);
            refreshTokenStore.delete(accountId); // 해당 계정 세션 전체 강제 무효화
            if (!loginAttemptLimiter.tryAcquire("reissue:" + accountId)) {
                throw new BusinessException(ErrorCode.TOO_MANY_ATTEMPTS);
            }
            throw new BusinessException(ErrorCode.TOKEN_REUSE_DETECTED);
        }
        if (result.status() == TokenValidationResult.NOT_FOUND) {
            if (!loginAttemptLimiter.tryAcquire("reissue:" + accountId)) {
                throw new BusinessException(ErrorCode.TOO_MANY_ATTEMPTS);
            }
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(accountId);
        log.info("[AUTH] 토큰 재발급 성공 - accountId: {}, 상태: {}", accountId, result.status());

        return MemberLoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(result.refreshToken())
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void logout(String accountId) {
        refreshTokenStore.delete(accountId);
        log.info("[AUTH] 로그아웃 - accountId: {}", accountId);
    }

    public TokenRenewResponseDto renewRefreshToken(String accountId) {
        String newRefreshToken = jwtTokenProvider.createRefreshToken(accountId);
        refreshTokenStore.save(accountId, newRefreshToken);
        log.info("[AUTH] RefreshToken 선제적 갱신 - accountId: {}", accountId);

        return TokenRenewResponseDto.builder()
                .refreshToken(newRefreshToken)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
