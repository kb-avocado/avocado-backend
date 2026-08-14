package com.avocado.domain.user.repository;

import com.avocado.global.security.jwt.component.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

/**
 * Refresh Token을 Redis에 보관한다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private static final String TOKEN_KEY_PREFIX = "auth:refresh:token:";
    private static final String SESSIONS_KEY_PREFIX = "auth:refresh:sessions:";

    private static final int TOKEN_BYTES = 32;

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 새 리프레시 토큰을 만들어 저장하고 원문(Refresh Token 그자체)을 반환한다.
     *
     * @param userId 토큰을 발급받을 회원
     * @return 쿠키에 담을 토큰 원문
     */
    public String issue(Long userId) {
        Duration ttl = jwtProperties.getRefreshTokenValidity();

        String token = generateToken();
        String hash = toHash(token);

        long now = System.currentTimeMillis();
        long expiresAt = now + ttl.toMillis();

        // Hash->User
        stringRedisTemplate.opsForValue()
                .set(tokenKey(hash), String.valueOf(userId), ttl);
        String sessionsKey = sessionsKey(userId);

        // ZSET은 TTL 걸 수 없음, 만료된 세션을 직접 걷어냄
        stringRedisTemplate.opsForZSet().removeRangeByScore(sessionsKey, 0, now);

        // USER->Hash
        stringRedisTemplate.opsForZSet()
                .add(sessionsKey, hash, expiresAt);
        stringRedisTemplate.expire(sessionsKey, ttl);

        return token;
    }


    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String toHash(String token) {
        try {
            byte[] hashed = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder(hashed.length * 2);

            for (byte b : hashed) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256을 사용할 수 없습니다.", e);
        }
    }

    private String tokenKey(String hash) {
        return TOKEN_KEY_PREFIX + hash;
    }

    private String sessionsKey(Long userId) {
        return SESSIONS_KEY_PREFIX + userId;
    }
}
