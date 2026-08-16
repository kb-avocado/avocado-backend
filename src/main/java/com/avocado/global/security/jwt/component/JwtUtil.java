package com.avocado.global.security.jwt.component;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

// 토큰을 HTTP 쿠키로 주고받는 부분을 담당
@Component
public class JwtUtil {

    public static final String ACCESS_TOKEN_COOKIE = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE = "refreshToken";

    // Path
    // Access 토큰은 모든 API 요청에 실려야 하지만, Refresh 토큰은 재발급, 로그아웃에만 필요해서 경로를 좁힌다.
    private static final String ACCESS_COOKIE_PATH = "/";
    private static final String REFRESH_COOKIE_PATH = "/api/auth";

    private final boolean secure;
    private final String sameSite;

    // 쿠키의 만료를 토큰과 맞춘다.
    private final Duration accessTokenValidity;
    private final Duration refreshTokenValidity;

    public JwtUtil(JwtProperties properties) {
        this.secure = properties.isCookieSecure();
        this.sameSite = properties.getCookieSameSite();
        this.accessTokenValidity = properties.getAccessTokenValidity();
        this.refreshTokenValidity = properties.getRefreshTokenValidity();
    }


    /* 공통 */
    // javax.servlet.Cookie는 SameSite 속성을 지원하지 않아 ResponseCookie로 헤더를 직접 구성
    private ResponseCookie buildCookie(
            String name, String value, Duration maxAge, String path
    ) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path(path)
                .maxAge(maxAge)
                .build();
    }

    // 로그인 성공 시 응답에 쿠키를 실어 보냄
    private void addCookie(
            HttpServletResponse response, String name, String value, Duration maxAge, String path
    ) {
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                buildCookie(name, value, maxAge, path).toString()
        );
    }

    // 요청에서 쿠키를 추출
    private Optional<String> resolveCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> name.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isEmpty())
                .findFirst();
    }


    /* 액세스 토큰 */
    // 로그인 성공 시 응답에 Access Token 쿠키를 실어 보냄
    public void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, accessTokenValidity, ACCESS_COOKIE_PATH);
    }

    // 로그아웃 시 Access Token 쿠키를 즉시 만료
    public void expireAccessTokenCookie(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", Duration.ZERO, ACCESS_COOKIE_PATH);
    }

    // 인증 필터에서 요청 쿠키의 Access Token을 꺼냄
    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        return resolveCookie(request, ACCESS_TOKEN_COOKIE);
    }


    /* 리프레시 토큰 */
    // 로그인 성공 시 응답에 Refresh Token 쿠키를 실어 보냄
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, refreshTokenValidity, REFRESH_COOKIE_PATH);
    }

    // 로그아웃 시 Refresh Token 쿠키를 즉시 만료
    public void expireRefreshTokenCookie(HttpServletResponse response) {
        addCookie(response, REFRESH_TOKEN_COOKIE, "", Duration.ZERO, REFRESH_COOKIE_PATH);
    }

    // 재발급 요청에서 쿠키의 Refresh Token을 꺼냄
    public Optional<String> resolveRefreshToken(HttpServletRequest request) {
        return resolveCookie(request, REFRESH_TOKEN_COOKIE);
    }

}
