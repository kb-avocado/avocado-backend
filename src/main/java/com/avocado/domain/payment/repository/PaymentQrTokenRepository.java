package com.avocado.domain.payment.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class PaymentQrTokenRepository {

    private static final String USER_TOKEN_KEY_PREFIX = "payment:qr:user:";
    private static final String TOKEN_USER_KEY_PREFIX = "payment:qr:token:";
    private static final String REISSUE_LOCK_KEY_PREFIX = "payment:qr:reissue-lock:";
    private static final String ACTIVE_TOKENS_KEY = "payment:qr:active-tokens";

    private final StringRedisTemplate stringRedisTemplate;

    public void save(
            Long userId,
            String token,
            Duration ttl,
            long expiresAtMillis
    ) {
        stringRedisTemplate.opsForValue()
                .set(userTokenKey(userId), token, ttl);
        stringRedisTemplate.opsForValue()
                .set(tokenUserKey(token), String.valueOf(userId), ttl);
        stringRedisTemplate.opsForZSet()
                .add(ACTIVE_TOKENS_KEY, token, expiresAtMillis);
    }

    public Optional<String> findTokenByUserId(Long userId) {
        return Optional.ofNullable(
                stringRedisTemplate.opsForValue().get(userTokenKey(userId))
        );
    }

    public Optional<Long> findUserIdByToken(String token) {
        return Optional.ofNullable(
                        stringRedisTemplate.opsForValue().get(tokenUserKey(token))
                )
                .map(Long::valueOf);
    }

    public void deleteByUserId(Long userId) {
        findTokenByUserId(userId).ifPresent(this::deleteToken);
        stringRedisTemplate.delete(userTokenKey(userId));
    }

    public void deleteToken(String token) {
        stringRedisTemplate.delete(tokenUserKey(token));
    }

    public long getTokenTtlSeconds(String token) {
        Long ttl = stringRedisTemplate.getExpire(
                tokenUserKey(token),
                TimeUnit.SECONDS
        );

        return ttl == null ? -1L : ttl;
    }

    public boolean acquireReissueLock(
            Long userId,
            Duration ttl
    ) {
        Boolean acquired = stringRedisTemplate.opsForValue()
                .setIfAbsent(reissueLockKey(userId), "1", ttl);

        return Boolean.TRUE.equals(acquired);
    }

    private String userTokenKey(Long userId) {
        return USER_TOKEN_KEY_PREFIX + userId;
    }

    private String tokenUserKey(String token) {
        return TOKEN_USER_KEY_PREFIX + token;
    }

    private String reissueLockKey(Long userId) {
        return REISSUE_LOCK_KEY_PREFIX + userId;
    }
}
