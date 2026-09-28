package com.ontheway.infra.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class LoginAttemptLimiter {
    private final Cache<String, AtomicInteger> attempts;
    private final int maxAttempts;

    public LoginAttemptLimiter(@Value("${rate-limit.max-attempts}") int maxAttempts,
                                @Value("${rate-limit.window-seconds}") long windowSeconds) {
        this.maxAttempts = maxAttempts;
        this.attempts = Caffeine.newBuilder()
                .expireAfterWrite(windowSeconds, TimeUnit.SECONDS)
                .build();
    }

    public boolean tryAcquire(String key) {
        return attempts.get(key, k -> new AtomicInteger(0)).incrementAndGet() <= maxAttempts;
    }
}
