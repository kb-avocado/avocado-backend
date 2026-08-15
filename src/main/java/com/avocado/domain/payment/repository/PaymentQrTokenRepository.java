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
    // POS 목록 조회용 인덱스. member는 token, score는 expiresAt epoch millis이다.
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
        // TTL이 있는 key와 별도로 ZSET에 만료 시각을 저장해야 목록 조회와 만료 정리가 가능하다.
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
                .flatMap(this::parseUserId);
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
        // score가 현재 시각 이하이면 결제 대기 시간이 끝난 토큰이다.
        Set<String> expiredTokens = stringRedisTemplate.opsForZSet()
                .rangeByScore(ACTIVE_TOKENS_KEY, Double.NEGATIVE_INFINITY, nowMillis);

        if (expiredTokens == null || expiredTokens.isEmpty()) {
            return 0;
        }

        expiredTokens.forEach(this::deleteToken);

        return expiredTokens.size();
    }

    public List<PaymentQrActiveTokenVo> findActiveTokens(long nowMillis) {
        // 만료되지 않은 score만 조회한다. cleanup 직후라도 Redis key TTL 만료와 ZSET 정리가 어긋날 수 있다.
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
                .map(activeToken -> toActiveToken(activeToken, nowMillis))
                .flatMap(Optional::stream)
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

    private Optional<Long> parseUserId(String value) {
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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

    private Optional<PaymentQrActiveTokenVo> toActiveToken(
            ZSetOperations.TypedTuple<String> activeToken,
            long nowMillis
    ) {
        String token = activeToken.getValue();
        Double expiresAtScore = activeToken.getScore();

        if (token == null || expiresAtScore == null) {
            return Optional.empty();
        }

        Optional<Long> userId = findUserIdByToken(token);
        if (userId.isEmpty()) {
            // token -> user 역인덱스가 없으면 이미 TTL 만료된 고아 ZSET member이므로 응답에서 제외한다.
            removeActiveToken(token);
            return Optional.empty();
        }

        long expiresAtMillis = expiresAtScore.longValue();
        long remainingMillis = Math.max(0L, expiresAtMillis - nowMillis);

        return Optional.of(PaymentQrActiveTokenVo.builder()
                .token(token)
                .userId(userId.get())
                .expiresIn((remainingMillis + 999L) / 1000L)
                .build());
    }
}
