package com.avocado.jwt.component;

import org.springframework.beans.factory.annotation.Value;
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

    private final boolean secure;
    private final String sameSite;

    public JwtUtil(
            @Value("${jwt.cookie.secure:false}") boolean secure,
            @Value("${jwt.cookie.same-site:Lax}") String sameSite
    ) {
        this.secure = secure;
        this.sameSite = sameSite;
    }

    // 로그인 성공 시 응답에 Access Token 쿠키를 실어 보냄
    public void addAccessTokenCookie(
            HttpServletResponse response,
            String accessToken,
            long validityMillis
    ) {
        ResponseCookie cookie = buildCookie(accessToken, Duration.ofMillis(validityMillis));
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 로그아웃 시 Access Token 쿠키를 즉시 만료
    public void expireAccessTokenCookie(HttpServletResponse response) {
        ResponseCookie cookie = buildCookie("", Duration.ZERO);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    // 인증 필터에서 요청 쿠키의 Access Token을 꺼냄
    public Optional<String> resolveAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> ACCESS_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(value -> value != null && !value.isEmpty())
                .findFirst();
    }

    // javax.servlet.Cookie는 SameSite 속성을 지원하지 않아 ResponseCookie로 헤더를 직접 구성
    private ResponseCookie buildCookie(
            String value,
            Duration maxAge
    ) {
        return ResponseCookie.from(ACCESS_TOKEN_COOKIE, value)
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
