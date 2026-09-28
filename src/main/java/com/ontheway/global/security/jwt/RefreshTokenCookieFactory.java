package com.ontheway.global.security.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieFactory {
    private static final String COOKIE_NAME = "refreshToken";

    private final long refreshTokenValidity;

    public RefreshTokenCookieFactory(@Value("${jwt.refresh-token-validity}") long refreshTokenValidity) {
        this.refreshTokenValidity = refreshTokenValidity;
    }

    public ResponseCookie create(String refreshToken) {
        return baseCookie(refreshToken)
                .maxAge(Duration.ofMillis(refreshTokenValidity))
                .build();
    }

    public ResponseCookie clear() {
        return baseCookie("")
                .maxAge(Duration.ZERO)
                .build();
    }

    private ResponseCookie.ResponseCookieBuilder baseCookie(String value) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/");
    }
}
