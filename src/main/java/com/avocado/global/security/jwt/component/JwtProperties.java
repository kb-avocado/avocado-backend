package com.avocado.global.security.jwt.component;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * JWT와 토큰 쿠키에 필요한 설정을 한곳에서 읽는다.
 */
@Getter
@Component
public class JwtProperties {

    private final String secret;
    private final String issuer;
    private final Duration accessTokenValidity;
    private final boolean cookieSecure;
    private final String cookieSameSite;

    public JwtProperties(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer:avocado}") String issuer,
            @Value("${jwt.access-token-validity:1800000}") long accessTokenValidityMillis,
            @Value("${jwt.cookie.secure:false}") boolean cookieSecure,
            @Value("${jwt.cookie.same-site:Lax}") String cookieSameSite
    ) {
        this.secret = secret;
        this.issuer = issuer;
        // 설정 파일에서는 밀리초로 받지만, 코드 안에서는 의미가 드러나는 Duration으로 다룬다.
        this.accessTokenValidity = Duration.ofMillis(accessTokenValidityMillis);
        this.cookieSecure = cookieSecure;
        this.cookieSameSite = cookieSameSite;
    }
}
