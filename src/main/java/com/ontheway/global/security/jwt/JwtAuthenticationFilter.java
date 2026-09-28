package com.ontheway.global.security.jwt;

import com.ontheway.infra.cache.RefreshTokenStore;
import com.ontheway.infra.cache.RotationResult;
import com.ontheway.infra.cache.TokenValidationResult;
import com.ontheway.service.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter { //jwt 검사
    private static final String REFRESH_COOKIE_NAME = "refreshToken";

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenStore refreshTokenStore;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String servletPath = request.getServletPath();
        if ("/user/login".equals(servletPath) || "/user/reissue".equals(servletPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = resolveAccessToken(request);

        if (accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // AccessToken 유효한 경우
        if (jwtTokenProvider.validateToken(accessToken) && "access".equals(jwtTokenProvider.getCategory(accessToken))) {
            authenticate(accessToken);
            filterChain.doFilter(request, response);
            return;
        }

        // AccessToken 만료 → RefreshToken 자동 갱신 시도
        if (jwtTokenProvider.isExpired(accessToken)) {
            String refreshToken = resolveRefreshTokenFromCookie(request);
            String newAccessToken = tryAutoRenew(refreshToken, response);

            if (newAccessToken != null) {
                // 갱신 성공 → 새 토큰으로 인증 처리 후 원래 요청 계속 진행
                authenticate(newAccessToken);
                filterChain.doFilter(request, response);
                return;
            }

            // RefreshToken도 없거나 만료/무효 → 로그아웃
            request.setAttribute("exception", "REFRESH_EXPIRED");
            filterChain.doFilter(request, response);
            return;
        }

        // 서명 위조 등 정상적인 만료가 아닌, 애초에 잘못된 토큰
        request.setAttribute("exception", "INVALID_TOKEN");
        filterChain.doFilter(request, response);
    }

    private String tryAutoRenew(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null
                || !jwtTokenProvider.validateToken(refreshToken)
                || !"refresh".equals(jwtTokenProvider.getCategory(refreshToken))) {
            return null;
        }

        String accountId = jwtTokenProvider.getAccountId(refreshToken);
        String candidateNewRefreshToken = jwtTokenProvider.createRefreshToken(accountId);
        RotationResult result = refreshTokenStore.rotate(accountId, refreshToken, candidateNewRefreshToken);

        if (result.status() == TokenValidationResult.REUSED) {
            log.warn("[SECURITY] RefreshToken 재사용 감지(필터 자동갱신) - accountId: {}", accountId);
            refreshTokenStore.delete(accountId);
            return null;
        }
        if (result.status() == TokenValidationResult.NOT_FOUND) {
            return null;
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(accountId);

        // VALID: candidateNewRefreshToken이 실제 저장됨
        // VALID_GRACE: 회전 없이 기존 current가 그대로 반환됨
        // → result.refreshToken()을 그대로 쿠키에 다시 세팅하면 두 경우 모두 안전
        setRefreshTokenCookie(response, result.refreshToken());
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + newAccessToken);

        log.info("[AUTH] 필터 자동 재발급 - accountId: {}, 상태: {}", accountId, result.status());
        return newAccessToken;
    }

    private void authenticate(String accessToken) {
        String accountId = jwtTokenProvider.getAccountId(accessToken);
        try {
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(accountId);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (UsernameNotFoundException e) {
            log.debug("토큰은 유효하나 존재하지 않는 계정: {}", accountId);
        }
    }

    private String resolveAccessToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String resolveRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (REFRESH_COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookieFactory.create(refreshToken).toString());
    }
}
