package com.avocado.domain.payment.service;

import com.avocado.domain.payment.domain.PaymentQrActiveTokenVo;
import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final long TOKEN_TTL_SECONDS = 180L;

    @Mock
    private PaymentQrTokenRepository paymentQrTokenRepository;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentQrTokenRepository);
        ReflectionTestUtils.setField(
                paymentService,
                "paymentQrTokenTtlSeconds",
                TOKEN_TTL_SECONDS
        );
        ReflectionTestUtils.setField(
                paymentService,
                "paymentQrReissueLockSeconds",
                3L
        );
    }

    @Test
    @DisplayName("로그인 사용자 기준 결제 QR 토큰을 발급하고 Redis에 TTL과 함께 저장한다")
    void issuePaymentQrToken_success() {
        // given
        AuthUser authUser = authUser(102L);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> expiresAtCaptor = ArgumentCaptor.forClass(Long.class);
        long beforeIssueExpiresAt = System.currentTimeMillis() + Duration.ofSeconds(TOKEN_TTL_SECONDS).toMillis();

        // when
        PaymentQrTokenResponseDto result = paymentService.issuePaymentQrToken(authUser);
        long afterIssueExpiresAt = System.currentTimeMillis() + Duration.ofSeconds(TOKEN_TTL_SECONDS).toMillis();

        // then
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresIn()).isEqualTo(TOKEN_TTL_SECONDS);

        verify(paymentQrTokenRepository).deleteByUserId(authUser.getUserId());
        verify(paymentQrTokenRepository).save(
                eq(authUser.getUserId()),
                tokenCaptor.capture(),
                eq(Duration.ofSeconds(TOKEN_TTL_SECONDS)),
                expiresAtCaptor.capture()
        );
        assertThat(tokenCaptor.getValue()).isEqualTo(result.getToken());
        assertThat(expiresAtCaptor.getValue())
                .isBetween(beforeIssueExpiresAt, afterIssueExpiresAt);
    }

    @Test
    @DisplayName("인증 정보가 없으면 QR 토큰을 발급하지 않는다")
    void issuePaymentQrToken_unauthenticated_fail() {
        // when & then
        assertThatThrownBy(() -> paymentService.issuePaymentQrToken(null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("QR 재발급 시 기존 토큰을 무효화하고 새 토큰을 저장한다")
    void reissuePaymentQrToken_success() {
        // given
        AuthUser authUser = authUser(102L);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> expiresAtCaptor = ArgumentCaptor.forClass(Long.class);

        when(paymentQrTokenRepository.acquireReissueLock(
                authUser.getUserId(),
                Duration.ofSeconds(3)
        )).thenReturn(true);
        long beforeIssueExpiresAt = System.currentTimeMillis() + Duration.ofSeconds(TOKEN_TTL_SECONDS).toMillis();

        // when
        PaymentQrTokenResponseDto result = paymentService.reissuePaymentQrToken(authUser);
        long afterIssueExpiresAt = System.currentTimeMillis() + Duration.ofSeconds(TOKEN_TTL_SECONDS).toMillis();

        // then
        verify(paymentQrTokenRepository).deleteByUserId(authUser.getUserId());
        verify(paymentQrTokenRepository).save(
                eq(authUser.getUserId()),
                tokenCaptor.capture(),
                eq(Duration.ofSeconds(TOKEN_TTL_SECONDS)),
                expiresAtCaptor.capture()
        );
        assertThat(tokenCaptor.getValue()).isEqualTo(result.getToken());
        assertThat(expiresAtCaptor.getValue())
                .isBetween(beforeIssueExpiresAt, afterIssueExpiresAt);
    }

    @Test
    @DisplayName("QR 재발급 요청이 너무 잦으면 기존 토큰을 무효화하지 않는다")
    void reissuePaymentQrToken_tooFrequent_fail() {
        // given
        AuthUser authUser = authUser(102L);

        when(paymentQrTokenRepository.acquireReissueLock(
                authUser.getUserId(),
                Duration.ofSeconds(3)
        )).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> paymentService.reissuePaymentQrToken(authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PAYMENT_QR_REISSUE_TOO_FREQUENT);

        verify(paymentQrTokenRepository, never()).deleteByUserId(authUser.getUserId());
    }

    @Test
    @DisplayName("POS 조회 전 만료 토큰을 정리하고 활성 QR 토큰 목록을 반환한다")
    void getActivePaymentQrTokens() {
        // given
        PaymentQrActiveTokenVo activeToken = PaymentQrActiveTokenVo.builder()
                .token("active-token")
                .userId(102L)
                .expiresIn(180L)
                .build();

        when(paymentQrTokenRepository.findActiveTokens(anyLong()))
                .thenReturn(List.of(activeToken));

        ArgumentCaptor<Long> cleanupNowCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> findNowCaptor = ArgumentCaptor.forClass(Long.class);

        // when
        List<PaymentQrActiveTokenResponseDto> result = paymentService.getActivePaymentQrTokens();

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getToken()).isEqualTo("active-token");
        assertThat(result.get(0).getUserId()).isEqualTo(102L);
        assertThat(result.get(0).getExpiresIn()).isEqualTo(180L);

        verify(paymentQrTokenRepository).cleanupExpiredTokens(cleanupNowCaptor.capture());
        verify(paymentQrTokenRepository).findActiveTokens(findNowCaptor.capture());
        assertThat(findNowCaptor.getValue()).isEqualTo(cleanupNowCaptor.getValue());
    }

    private AuthUser authUser(Long userId) {
        return AuthUser.builder()
                .userId(userId)
                .role(UserRole.USER)
                .userType(UserType.CHILD)
                .build();
    }
}
