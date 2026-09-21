package com.ontheway.infra.cache;

public interface RefreshTokenStore {
    void save(String accountId, String refreshToken);
    boolean matches(String accountId, String refreshToken);
    void delete(String accountId);
}
