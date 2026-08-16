package com.avocado.domain.payment.repository;

import com.avocado.domain.payment.domain.PaymentQrActiveTokenVo;
import com.avocado.domain.payment.domain.PaymentQrStatus;
import com.avocado.domain.payment.domain.PaymentQrStatusVo;
import com.avocado.domain.payment.domain.PaymentSimulationResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class PaymentQrTokenRepository {

    private static final String USER_TOKEN_KEY_PREFIX = "payment:qr:user:";
    private static final String TOKEN_USER_KEY_PREFIX = "payment:qr:token:";
    private static final String TOKEN_STATUS_KEY_PREFIX = "payment:qr:status:";
    private static final String REISSUE_LOCK_KEY_PREFIX = "payment:qr:reissue-lock:";
    // POS 목록 조회용 인덱스. member는 token, score는 expiresAt epoch millis이다.
    private static final String ACTIVE_TOKENS_KEY = "payment:qr:active-tokens";
    private static final String STATUS_FIELD = "status";
    private static final String USER_ID_FIELD = "userId";
    private static final String EXPIRES_AT_MILLIS_FIELD = "expiresAtMillis";
    private static final String WALLET_HISTORY_ID_FIELD = "walletHistoryId";
    private static final String MERCHANT_ID_FIELD = "merchantId";
    private static final String MERCHANT_NAME_FIELD = "merchantName";
    private static final String AMOUNT_FIELD = "amount";
    private static final String FAILURE_CODE_FIELD = "failureCode";
    private static final String BALANCE_AFTER_FIELD = "balanceAfter";
    private static final RedisScript<String> CONSUME_TOKEN_SCRIPT = new DefaultRedisScript<>(
            """
            local userId = redis.call('GET', KEYS[1])
            if not userId then
                return nil
            end

            local userTokenKey = ARGV[1] .. userId
            local currentToken = redis.call('GET', userTokenKey)
            if currentToken == ARGV[2] then
                redis.call('DEL', userTokenKey)
            end

            redis.call('DEL', KEYS[1])
            return userId
            """,
            String.class
    );

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

    public void saveWaitingStatus(
            Long userId,
            String token,
            Duration ttl,
            long expiresAtMillis
    ) {
        Map<String, String> statusFields = new HashMap<>();
        statusFields.put(STATUS_FIELD, PaymentQrStatus.WAITING.name());
        statusFields.put(USER_ID_FIELD, String.valueOf(userId));
        statusFields.put(EXPIRES_AT_MILLIS_FIELD, String.valueOf(expiresAtMillis));

        saveStatus(token, statusFields, ttl);
    }

    public void saveExpiredStatus(
            String token,
            Long userId,
            Duration ttl
    ) {
        Map<String, String> statusFields = new HashMap<>();
        statusFields.put(STATUS_FIELD, PaymentQrStatus.EXPIRED.name());
        statusFields.put(USER_ID_FIELD, String.valueOf(userId));

        saveStatus(token, statusFields, ttl);
    }

    public void saveCompletedStatus(
            String token,
            Long userId,
            PaymentSimulationResult result,
            Duration ttl
    ) {
        Map<String, String> statusFields = new HashMap<>();
        statusFields.put(STATUS_FIELD, result.getStatus());
        statusFields.put(USER_ID_FIELD, String.valueOf(userId));
        putIfNotNull(statusFields, WALLET_HISTORY_ID_FIELD, result.getWalletHistoryId());
        putIfNotNull(statusFields, MERCHANT_ID_FIELD, result.getMerchantId());
        putIfNotNull(statusFields, MERCHANT_NAME_FIELD, result.getMerchantName());
        putIfNotNull(statusFields, AMOUNT_FIELD, result.getAmount());
        putIfNotNull(statusFields, FAILURE_CODE_FIELD, result.getFailureCode() == null
                ? null
                : result.getFailureCode().name());
        putIfNotNull(statusFields, BALANCE_AFTER_FIELD, result.getBalanceAfter());

        saveStatus(token, statusFields, ttl);
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

    public Optional<PaymentQrStatusVo> findStatusByToken(String token) {
        Map<Object, Object> statusFields = stringRedisTemplate.opsForHash()
                .entries(tokenStatusKey(token));

        if (statusFields == null || statusFields.isEmpty()) {
            return Optional.empty();
        }

        Optional<PaymentQrStatus> status = parseStatus(
                getString(statusFields, STATUS_FIELD)
        );

        if (status.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(PaymentQrStatusVo.builder()
                .status(status.get())
                .userId(parseLong(getString(statusFields, USER_ID_FIELD)).orElse(null))
                .expiresAtMillis(parseLong(getString(statusFields, EXPIRES_AT_MILLIS_FIELD)).orElse(null))
                .walletHistoryId(parseLong(getString(statusFields, WALLET_HISTORY_ID_FIELD)).orElse(null))
                .merchantId(parseLong(getString(statusFields, MERCHANT_ID_FIELD)).orElse(null))
                .merchantName(getString(statusFields, MERCHANT_NAME_FIELD))
                .amount(parseLong(getString(statusFields, AMOUNT_FIELD)).orElse(null))
                .failureCode(getString(statusFields, FAILURE_CODE_FIELD))
                .balanceAfter(parseLong(getString(statusFields, BALANCE_AFTER_FIELD)).orElse(null))
                .build());
    }

    public Optional<Long> consumeUserIdByToken(String token) {
        String userId = stringRedisTemplate.execute(
                CONSUME_TOKEN_SCRIPT,
                List.of(tokenUserKey(token)),
                USER_TOKEN_KEY_PREFIX,
                token
        );

        if (userId != null) {
            removeActiveToken(token);
        }

        return Optional.ofNullable(userId)
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

    private String tokenStatusKey(String token) {
        return TOKEN_STATUS_KEY_PREFIX + token;
    }

    private String reissueLockKey(Long userId) {
        return REISSUE_LOCK_KEY_PREFIX + userId;
    }

    private void saveStatus(
            String token,
            Map<String, String> statusFields,
            Duration ttl
    ) {
        String statusKey = tokenStatusKey(token);

        stringRedisTemplate.delete(statusKey);
        stringRedisTemplate.opsForHash()
                .putAll(statusKey, statusFields);
        stringRedisTemplate.expire(statusKey, ttl);
    }

    private void putIfNotNull(
            Map<String, String> statusFields,
            String key,
            Object value
    ) {
        if (value != null) {
            statusFields.put(key, String.valueOf(value));
        }
    }

    private String getString(
            Map<Object, Object> statusFields,
            String field
    ) {
        Object value = statusFields.get(field);

        return value == null ? null : value.toString();
    }

    private Optional<Long> parseUserId(String value) {
        return parseLong(value);
    }

    private Optional<Long> parseLong(String value) {
        try {
            return Optional.of(Long.valueOf(value));
        } catch (NumberFormatException | NullPointerException e) {
            return Optional.empty();
        }
    }

    private Optional<PaymentQrStatus> parseStatus(String value) {
        try {
            return Optional.of(PaymentQrStatus.valueOf(value));
        } catch (IllegalArgumentException | NullPointerException e) {
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
