package com.ontheway.global.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class RateLimitExceededException extends AuthenticationException {
    public RateLimitExceededException(String msg) {
        super(msg);
    }
}
