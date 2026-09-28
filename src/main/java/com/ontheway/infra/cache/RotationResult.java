package com.ontheway.infra.cache;

public record RotationResult(TokenValidationResult status, String refreshToken) {
}
