package com.ontheway.infra.cache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CaffeineRefreshTokenStore implements RefreshTokenStore {
    private static final long GRACE_PERIOD_MILLIS = 5_000;
    private final Cache<String, TokenRecord> refreshTokenCache;
    private record TokenRecord(String currentToken, String previousToken, long rotatedAtMillis) {}

    public CaffeineRefreshTokenStore(@Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        this.refreshTokenCache = Caffeine.newBuilder()
                .expireAfterWrite(refreshTokenValidity, TimeUnit.MILLISECONDS)
                .maximumSize(100_000)
                .build();
    }

    @Override
    public void save(String accountId, String refreshToken) {
        refreshTokenCache.asMap().compute(accountId, (id, record) -> {
            String previous = (record != null) ? record.currentToken() : null;
            return new TokenRecord(refreshToken, previous, System.currentTimeMillis());
        });
    }

    @Override
    public void saveOnLogin(String accountId, String refreshToken) {
        refreshTokenCache.put(accountId, new TokenRecord(refreshToken, null, System.currentTimeMillis()));
    }

    @Override
    public RotationResult rotate(String accountId, String presentedRefreshToken, String candidateNewRefreshToken) {
        TokenValidationResult[] status = new TokenValidationResult[1];
        String[] effectiveToken = new String[1];

        refreshTokenCache.asMap().compute(accountId, (id, record) -> {
            if (record == null) {
                status[0] = TokenValidationResult.NOT_FOUND;
                return null;
            }
            if (presentedRefreshToken.equals(record.currentToken())) {
                status[0] = TokenValidationResult.VALID;
                effectiveToken[0] = candidateNewRefreshToken;
                return new TokenRecord(candidateNewRefreshToken, record.currentToken(), System.currentTimeMillis());
            }

            boolean isPreviousToken = presentedRefreshToken.equals(record.previousToken());
            boolean withinGracePeriod = (System.currentTimeMillis() - record.rotatedAtMillis()) <= GRACE_PERIOD_MILLIS;

            if (isPreviousToken && withinGracePeriod) {
                status[0] = TokenValidationResult.VALID_GRACE;
                effectiveToken[0] = record.currentToken(); // 이미 회전된 결과 재사용, 재회전하지 않음
                return record; // 캐시 상태 변경 없음
            }

            status[0] = TokenValidationResult.REUSED; //탈취 의심
            return record;
        });

        return new RotationResult(status[0], effectiveToken[0]);
    }

    @Override
    public void delete(String accountId) {
        refreshTokenCache.invalidate(accountId);
    }
}
