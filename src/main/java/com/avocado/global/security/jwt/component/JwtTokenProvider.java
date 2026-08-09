package com.avocado.global.security.jwt.component;

import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

// JWT 발급과 검증을 담당. jjwt 의존은 이 클래스 안에서만 사용한다.
@Slf4j
@Component
public class JwtTokenProvider {

    public static final String CLAIM_ROLE = "role";
    public static final String CLAIM_USER_TYPE = "userType";

    private final SecretKey secretKey;
    private final String issuer;

    @Getter
    private final long accessTokenValidity;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.issuer:avocado}") String issuer,
            @Value("${jwt.access-token-validity:1800000}") long accessTokenValidity
    ) {
        // HS256은 32바이트 미만 키를 거부하므로 Base64 디코딩 결과 길이에 주의
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.issuer = issuer;
        this.accessTokenValidity = accessTokenValidity;
    }

    /**
     * 로그인 성공 시 Access Token을 발급.
     *
     * @param userId   회원 ID (sub 클레임)
     * @param role     회원 권한 (인가 판단에 사용)
     * @param userType 회원 타입 (부모/아이 구분, 비즈니스 로직에 사용)
     * @return 서명된 JWT 문자열
     */
    public String createAccessToken(
            Long userId,
            UserRole role,
            UserType userType
    ) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenValidity);

        return Jwts.builder()
                .issuer(issuer)
                .subject(String.valueOf(userId))
                .claim(CLAIM_ROLE, role.name())
                .claim(CLAIM_USER_TYPE, userType.name())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰의 서명과 만료를 검증하고 클레임을 반환.
     *
     * @param token JWT 문자열
     * @return 유효하면 Claims, 유효하지 않으면 null
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .requireIssuer(issuer)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("유효하지 않은 JWT: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 토큰을 검증하고 인증 주체로 변환. 인증 필터가 사용한다.
     *
     * @param token JWT 문자열
     * @return 유효하면 AuthUser, 유효하지 않으면 null
     */
    public AuthUser parseAuthUser(String token) {
        Claims claims = parseClaims(token);

        if (claims == null) {
            return null;
        }

        try {
            return AuthUser.builder()
                    .userId(Long.valueOf(claims.getSubject()))
                    .role(UserRole.valueOf(claims.get(CLAIM_ROLE, String.class)))
                    .userType(UserType.valueOf(claims.get(CLAIM_USER_TYPE, String.class)))
                    .build();
        } catch (IllegalArgumentException | NullPointerException e) {
            // 서명은 유효하지만 클레임 구성이 바뀐 경우 (예: enum 값 변경 후 남아있는 옛 토큰)
            log.debug("JWT 클레임을 해석할 수 없습니다: {}", e.getMessage());
            return null;
        }
    }
}
