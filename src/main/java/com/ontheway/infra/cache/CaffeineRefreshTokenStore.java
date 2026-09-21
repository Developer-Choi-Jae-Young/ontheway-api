package com.ontheway.infra.cache;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CaffeineRefreshTokenStore implements RefreshTokenStore {

    private final Cache<String, String> refreshTokenCache;

    public CaffeineRefreshTokenStore(@Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        this.refreshTokenCache = Caffeine.newBuilder()
                .expireAfterWrite(refreshTokenValidity, TimeUnit.MILLISECONDS)
                .maximumSize(100_000)
                .build();
    }

    @Override
    public void save(String accountId, String refreshToken) {
        refreshTokenCache.put(accountId, refreshToken);
    }

    @Override
    public boolean matches(String accountId, String refreshToken) {
        String saved = refreshTokenCache.getIfPresent(accountId);
        return saved != null && saved.equals(refreshToken);
    }

    @Override
    public void delete(String accountId) {
        refreshTokenCache.invalidate(accountId);
    }
}
