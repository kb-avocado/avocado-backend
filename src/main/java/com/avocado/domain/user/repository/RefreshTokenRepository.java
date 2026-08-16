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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Refresh Token을 Redis에 보관한다.
 */
@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {
    private static final String TOKEN_KEY_PREFIX = "auth:refresh:token:";
    private static final String SESSIONS_KEY_PREFIX = "auth:refresh:sessions:";
    private static final String USED_KEY_PREFIX = "auth:refresh:used:";

    private static final int TOKEN_BYTES = 32;

    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    private final SecureRandom secureRandom = new SecureRandom();

    private String tokenKey(String hash) {
        return TOKEN_KEY_PREFIX + hash;
    }

    private String sessionsKey(Long userId) {
        return SESSIONS_KEY_PREFIX + userId;
    }

    private String usedKey(String hash) {
        return USED_KEY_PREFIX + hash;
    }

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


    /**
     * 유효한 리프레시 토큰인지 확인하고, 유효하다면 주인을 반환한다. (유효하면 정상)
     *
     * @return userId. 만료됐거나 없는 토큰이면 비어있다.
     */
    public Optional<Long> findUserId(String token) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(tokenKey(toHash(token))))
                .map(Long::valueOf);
    }

    /**
     * 이미 회전되어 폐기된 토큰이면 주인을 반환한다. (유효하면 탈취)
     *
     * @return userId. 회전되지 않은 토큰이면 비어있다.
     */
    public Optional<Long> findUsedUserId(String token) {
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(usedKey(toHash(token))))
                .map(Long::valueOf);
    }

    /**
     * 회전: 옛 토큰을 폐기하고 새 토큰을 발급한다.
     * 옛 토큰은 'used'로 남긴다. 토큰이 탈취되었는지 그냥 없는 토큰인지 구분할 수 있다.
     *
     * @return 쿠키에 담을 새 토큰 원문
     */
    public String rotate(Long userId, String oldToken) {
        String oldHash = toHash(oldToken);

        Long remainingSeconds = stringRedisTemplate.getExpire(tokenKey(oldHash), TimeUnit.SECONDS);

        if (remainingSeconds != null && remainingSeconds > 0) {
            stringRedisTemplate.opsForValue()
                    .set(usedKey(oldHash), String.valueOf(userId), Duration.ofSeconds(remainingSeconds));
        }

        stringRedisTemplate.delete(tokenKey(oldHash));
        stringRedisTemplate.opsForZSet().remove(sessionsKey(userId), oldHash);

        return issue(userId);
    }

    /**
     * 한 세션만 끊음 (로그아웃)
     */
    public void revoke(String token) {
        String hash = toHash(token);
        String userId = stringRedisTemplate.opsForValue().get(tokenKey(hash));

        stringRedisTemplate.delete(tokenKey(hash));

        if (userId != null) {
            stringRedisTemplate.opsForZSet().remove(sessionsKey(Long.valueOf(userId)), hash);
        }
    }

    /**
     * 이 회원의 모든 세션을 끊는다. (토큰 재사용 감지)
     */
    public void revokeAll(Long userId) {
        String sessionsKey = sessionsKey(userId);

        Set<String> hashes = stringRedisTemplate.opsForZSet().range(sessionsKey, 0, -1);

        if (hashes != null) {
            for (String hash: hashes) {
                stringRedisTemplate.delete(tokenKey(hash));
            }
        }
        stringRedisTemplate.delete(sessionsKey);
    }
}
