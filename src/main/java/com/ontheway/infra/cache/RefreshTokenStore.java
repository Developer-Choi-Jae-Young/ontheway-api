package com.ontheway.infra.cache;

public interface RefreshTokenStore {
    void save(String accountId, String refreshToken); //refresh-token/renew 갱신
    void saveOnLogin(String accountId, String refreshToken); //로그인 -> 생성
    RotationResult rotate(String accountId, String presentedRefreshToken, String candidateNewRefreshToken);
    void delete(String accountId);
}
