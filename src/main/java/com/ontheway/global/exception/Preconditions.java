package com.ontheway.global.exception;

/** 조건이 깨지면 해당 {@link ErrorCode} 로 {@link BusinessException} 을 던진다. static import 해서 쓴다. */
public final class Preconditions {

    private Preconditions() {
    }

    public static void require(boolean condition, ErrorCode errorCode) {
        if (!condition) {
            throw new BusinessException(errorCode);
        }
    }
}
