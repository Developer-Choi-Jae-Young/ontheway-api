package com.ontheway.service;

import com.ontheway.dto.request.TokenReissueRequestDto;
import com.ontheway.dto.response.MemberLoginResponseDto;
import com.ontheway.global.exception.BusinessException;
import com.ontheway.global.exception.ErrorCode;
import com.ontheway.global.security.jwt.JwtTokenProvider;
import com.ontheway.infra.cache.RefreshTokenStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public MemberLoginResponseDto reissue(TokenReissueRequestDto dto) {
        String refreshToken = dto.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            if (jwtTokenProvider.isExpired(refreshToken)) {
                throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
            }
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String accountId = jwtTokenProvider.getAccountId(refreshToken);

        if (!refreshTokenStore.matches(accountId, refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(accountId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(accountId);

        refreshTokenStore.save(accountId, newRefreshToken);

        return MemberLoginResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void logout(String accountId) {
        refreshTokenStore.delete(accountId);
    }

}
