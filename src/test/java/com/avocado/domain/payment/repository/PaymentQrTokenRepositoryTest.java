package com.avocado.domain.payment.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentQrTokenRepositoryTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private PaymentQrTokenRepository paymentQrTokenRepository;

    @BeforeEach
    void setUp() {
        paymentQrTokenRepository = new PaymentQrTokenRepository(stringRedisTemplate);
    }

    @Test
    @DisplayName("사용자 토큰과 토큰 사용자 역인덱스를 TTL과 함께 저장한다")
    void save_withTtl() {
        // given
        Long userId = 102L;
        String token = "qr-token";
        Duration ttl = Duration.ofSeconds(180);
        long expiresAtMillis = 1797220000000L;

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(stringRedisTemplate.opsForZSet()).thenReturn(zSetOperations);

        // when
        paymentQrTokenRepository.save(userId, token, ttl, expiresAtMillis);

        // then
        verify(valueOperations).set("payment:qr:user:102", token, ttl);
        verify(valueOperations).set("payment:qr:token:qr-token", "102", ttl);
        verify(zSetOperations).add("payment:qr:active-tokens", token, expiresAtMillis);
    }

    @Test
    @DisplayName("사용자 ID로 저장된 QR 토큰을 조회한다")
    void findTokenByUserId() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("payment:qr:user:102")).thenReturn("qr-token");

        // when
        Optional<String> result = paymentQrTokenRepository.findTokenByUserId(102L);

        // then
        assertThat(result).contains("qr-token");
    }

    @Test
    @DisplayName("토큰으로 사용자 ID를 조회한다")
    void findUserIdByToken() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("payment:qr:token:qr-token")).thenReturn("102");

        // when
        Optional<Long> result = paymentQrTokenRepository.findUserIdByToken("qr-token");

        // then
        assertThat(result).contains(102L);
    }

    @Test
    @DisplayName("사용자 기준 삭제 시 기존 토큰 역인덱스와 사용자 토큰을 모두 삭제한다")
    void deleteByUserId() {
        // given
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("payment:qr:user:102")).thenReturn("qr-token");

        // when
        paymentQrTokenRepository.deleteByUserId(102L);

        // then
        verify(stringRedisTemplate).delete("payment:qr:token:qr-token");
        verify(stringRedisTemplate).delete("payment:qr:user:102");
    }

    @Test
    @DisplayName("토큰 TTL을 초 단위로 조회한다")
    void getTokenTtlSeconds() {
        // given
        when(stringRedisTemplate.getExpire("payment:qr:token:qr-token", TimeUnit.SECONDS))
                .thenReturn(179L);

        // when
        long result = paymentQrTokenRepository.getTokenTtlSeconds("qr-token");

        // then
        assertThat(result).isEqualTo(179L);
    }

    @Test
    @DisplayName("재발급 lock을 TTL과 함께 획득한다")
    void acquireReissueLock_success() {
        // given
        Duration ttl = Duration.ofSeconds(3);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("payment:qr:reissue-lock:102", "1", ttl))
                .thenReturn(true);

        // when
        boolean result = paymentQrTokenRepository.acquireReissueLock(102L, ttl);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("재발급 lock이 이미 있으면 획득하지 못한다")
    void acquireReissueLock_alreadyExists() {
        // given
        Duration ttl = Duration.ofSeconds(3);

        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.setIfAbsent("payment:qr:reissue-lock:102", "1", ttl))
                .thenReturn(false);

        // when
        boolean result = paymentQrTokenRepository.acquireReissueLock(102L, ttl);

        // then
        assertThat(result).isFalse();
    }
}
