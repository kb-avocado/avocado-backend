package com.avocado.domain.payment.repository;

import com.avocado.domain.payment.domain.PaymentQrActiveTokenVo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
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
        findTokenByUserId(userId).ifPresent(token -> {
            stringRedisTemplate.delete(tokenUserKey(token));
            removeActiveToken(token);
        });
        stringRedisTemplate.delete(userTokenKey(userId));
    }

    public void deleteToken(String token) {
        findUserIdByToken(token)
                .ifPresent(userId -> deleteUserTokenIfCurrent(userId, token));
        stringRedisTemplate.delete(tokenUserKey(token));
        removeActiveToken(token);
    }

    public int cleanupExpiredTokens(long nowMillis) {
        Set<String> expiredTokens = stringRedisTemplate.opsForZSet()
                .rangeByScore(ACTIVE_TOKENS_KEY, Double.NEGATIVE_INFINITY, nowMillis);

        if (expiredTokens == null || expiredTokens.isEmpty()) {
            return 0;
        }

        expiredTokens.forEach(this::deleteToken);

        return expiredTokens.size();
    }

    public List<PaymentQrActiveTokenVo> findActiveTokens(long nowMillis) {
        Set<ZSetOperations.TypedTuple<String>> activeTokens = stringRedisTemplate.opsForZSet()
                .rangeByScoreWithScores(
                        ACTIVE_TOKENS_KEY,
                        nowMillis + 1,
                        Double.POSITIVE_INFINITY
                );

        if (activeTokens == null || activeTokens.isEmpty()) {
            return List.of();
        }

        return activeTokens.stream()
                .filter(activeToken -> activeToken.getValue() != null)
                .filter(activeToken -> activeToken.getScore() != null)
                .map(activeToken -> toActiveToken(activeToken, nowMillis))
                .toList();
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

    private void deleteUserTokenIfCurrent(
            Long userId,
            String token
    ) {
        findTokenByUserId(userId)
                .filter(token::equals)
                .ifPresent(currentToken -> stringRedisTemplate.delete(userTokenKey(userId)));
    }

    private void removeActiveToken(String token) {
        stringRedisTemplate.opsForZSet()
                .remove(ACTIVE_TOKENS_KEY, token);
    }

    private PaymentQrActiveTokenVo toActiveToken(
            ZSetOperations.TypedTuple<String> activeToken,
            long nowMillis
    ) {
        long expiresAtMillis = activeToken.getScore().longValue();
        long remainingMillis = Math.max(0L, expiresAtMillis - nowMillis);

        return PaymentQrActiveTokenVo.builder()
                .token(activeToken.getValue())
                .expiresAt(expiresAtMillis)
                .expiresIn((remainingMillis + 999L) / 1000L)
                .build();
    }
}
