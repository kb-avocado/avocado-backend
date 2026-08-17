package com.avocado.domain.payment.service;

import com.avocado.domain.payment.domain.PaymentQrActiveTokenVo;
import com.avocado.domain.payment.domain.PaymentRequestedResult;
import com.avocado.domain.payment.domain.PaymentQrStatus;
import com.avocado.domain.payment.domain.PaymentQrStatusVo;
import com.avocado.domain.payment.domain.PaymentSimulationResult;
import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrStatusResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.wallet.service.WalletService;
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
import java.util.Optional;

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

    @Mock
    private WalletService walletService;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentServiceImpl(paymentQrTokenRepository, walletService);
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
        ReflectionTestUtils.setField(
                paymentService,
                "paymentQrStatusRetentionSeconds",
                60L
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

        when(paymentQrTokenRepository.findTokenByUserId(authUser.getUserId()))
                .thenReturn(Optional.empty());

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
        verify(paymentQrTokenRepository).saveWaitingStatus(
                authUser.getUserId(),
                result.getToken(),
                Duration.ofSeconds(240),
                expiresAtCaptor.getValue()
        );
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
        when(paymentQrTokenRepository.findTokenByUserId(authUser.getUserId()))
                .thenReturn(Optional.empty());
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
        verify(paymentQrTokenRepository).saveWaitingStatus(
                authUser.getUserId(),
                result.getToken(),
                Duration.ofSeconds(240),
                expiresAtCaptor.getValue()
        );
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
    @DisplayName("QR 상태 조회 시 대기 중인 토큰이면 WAITING과 남은 시간을 반환한다")
    void getPaymentQrStatus_waiting() {
        // given
        AuthUser authUser = authUser(102L);
        long expiresAtMillis = System.currentTimeMillis() + Duration.ofSeconds(180).toMillis();
        PaymentQrStatusVo savedStatus = PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.WAITING)
                .userId(102L)
                .expiresAtMillis(expiresAtMillis)
                .build();

        when(paymentQrTokenRepository.findStatusByToken("qr-token"))
                .thenReturn(Optional.of(savedStatus));

        // when
        PaymentQrStatusResponseDto result = paymentService.getPaymentQrStatus(
                authUser,
                "qr-token"
        );

        // then
        assertThat(result.getStatus()).isEqualTo("WAITING");
        assertThat(result.getExpiresIn()).isBetween(179L, 180L);
    }

    @Test
    @DisplayName("QR 상태 조회 시 결제 완료 상태면 결제 결과를 반환한다")
    void getPaymentQrStatus_success() {
        // given
        AuthUser authUser = authUser(102L);
        PaymentQrStatusVo savedStatus = PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.SUCCESS)
                .userId(102L)
                .walletHistoryId(9001L)
                .merchantId(3001L)
                .merchantName("아보카도 편의점")
                .amount(12000L)
                .balanceAfter(36000L)
                .build();

        when(paymentQrTokenRepository.findStatusByToken("qr-token"))
                .thenReturn(Optional.of(savedStatus));

        // when
        PaymentQrStatusResponseDto result = paymentService.getPaymentQrStatus(
                authUser,
                "qr-token"
        );

        // then
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getWalletHistoryId()).isEqualTo(9001L);
        assertThat(result.getMerchantName()).isEqualTo("아보카도 편의점");
        assertThat(result.getAmount()).isEqualTo(12000L);
        assertThat(result.getBalanceAfter()).isEqualTo(36000L);
    }

    @Test
    @DisplayName("QR 상태 조회 시 만료 시간이 지난 대기 토큰이면 EXPIRED를 반환한다")
    void getPaymentQrStatus_expired() {
        // given
        AuthUser authUser = authUser(102L);
        PaymentQrStatusVo savedStatus = PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.WAITING)
                .userId(102L)
                .expiresAtMillis(System.currentTimeMillis() - 1L)
                .build();

        when(paymentQrTokenRepository.findStatusByToken("qr-token"))
                .thenReturn(Optional.of(savedStatus));

        // when
        PaymentQrStatusResponseDto result = paymentService.getPaymentQrStatus(
                authUser,
                "qr-token"
        );

        // then
        assertThat(result.getStatus()).isEqualTo("EXPIRED");
        assertThat(result.getExpiresIn()).isZero();
    }

    @Test
    @DisplayName("QR 상태 조회 시 다른 사용자의 토큰이면 INVALID를 반환한다")
    void getPaymentQrStatus_otherUser_invalid() {
        // given
        AuthUser authUser = authUser(102L);
        PaymentQrStatusVo savedStatus = PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.WAITING)
                .userId(103L)
                .expiresAtMillis(System.currentTimeMillis() + Duration.ofSeconds(180).toMillis())
                .build();

        when(paymentQrTokenRepository.findStatusByToken("qr-token"))
                .thenReturn(Optional.of(savedStatus));

        // when
        PaymentQrStatusResponseDto result = paymentService.getPaymentQrStatus(
                authUser,
                "qr-token"
        );

        // then
        assertThat(result.getStatus()).isEqualTo("INVALID");
        assertThat(result.getExpiresIn()).isZero();
    }

    @Test
    @DisplayName("저장된 QR 상태가 없어도 활성 토큰이면 WAITING을 반환한다")
    void getPaymentQrStatus_activeWithoutSavedStatus() {
        // given
        AuthUser authUser = authUser(102L);

        when(paymentQrTokenRepository.findStatusByToken("qr-token"))
                .thenReturn(Optional.empty());
        when(paymentQrTokenRepository.findUserIdByToken("qr-token"))
                .thenReturn(Optional.of(102L));
        when(paymentQrTokenRepository.getTokenTtlSeconds("qr-token"))
                .thenReturn(120L);

        // when
        PaymentQrStatusResponseDto result = paymentService.getPaymentQrStatus(
                authUser,
                "qr-token"
        );

        // then
        assertThat(result.getStatus()).isEqualTo("WAITING");
        assertThat(result.getExpiresIn()).isEqualTo(120L);
    }

    @Test
    @DisplayName("QR 상태 조회 시 인증 정보가 없으면 실패한다")
    void getPaymentQrStatus_unauthenticated_fail() {
        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentQrStatus(null, "qr-token"))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);
    }

    @Test
    @DisplayName("QR 상태 조회 시 빈 토큰이면 실패한다")
    void getPaymentQrStatus_blankToken_fail() {
        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentQrStatus(authUser(102L), " "))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    @Test
    @DisplayName("QR 토큰이 없거나 만료되면 지갑 결제를 호출하지 않고 실패 결과를 반환한다")
    void simulatePayment_invalidQr_failResult() {
        // given
        PaymentSimulationRequestDto request = paymentSimulationRequest();

        when(paymentQrTokenRepository.consumeUserIdByToken("qr-token"))
                .thenReturn(Optional.empty());

        // when
        PaymentSimulationResponseDto result = paymentService.simulatePayment(request);

        // then
        assertThat(result.getWalletHistoryId()).isNull();
        assertThat(result.getMerchantId()).isEqualTo(3001L);
        assertThat(result.getAmount()).isEqualTo(12000L);
        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureCode()).isEqualTo("INVALID_OR_EXPIRED_QR");
        assertThat(result.getBalanceAfter()).isNull();

        verify(walletService, never()).processPosPayment(
                eq(102L),
                eq(3001L),
                eq(12000L),
                eq(PaymentRequestedResult.SUCCESS)
        );
    }

    @Test
    @DisplayName("QR 토큰을 소비하면 사용자 ID로 지갑 결제를 위임한다")
    void simulatePayment_delegateWalletPayment() {
        // given
        PaymentSimulationRequestDto request = paymentSimulationRequest();
        PaymentSimulationResult walletResult = PaymentSimulationResult.builder()
                .walletHistoryId(10L)
                .merchantId(3001L)
                .merchantName("아보카도 편의점")
                .amount(12000L)
                .status("SUCCESS")
                .balanceAfter(36000L)
                .build();

        when(paymentQrTokenRepository.consumeUserIdByToken("qr-token"))
                .thenReturn(Optional.of(102L));
        when(walletService.processPosPayment(
                102L,
                3001L,
                12000L,
                PaymentRequestedResult.SUCCESS
        )).thenReturn(walletResult);

        // when
        PaymentSimulationResponseDto result = paymentService.simulatePayment(request);

        // then
        assertThat(result.getWalletHistoryId()).isEqualTo(10L);
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getBalanceAfter()).isEqualTo(36000L);

        verify(paymentQrTokenRepository).saveCompletedStatus(
                "qr-token",
                102L,
                walletResult,
                Duration.ofSeconds(60)
        );
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

    private PaymentSimulationRequestDto paymentSimulationRequest() {
        PaymentSimulationRequestDto request = new PaymentSimulationRequestDto();
        request.setQrToken("qr-token");
        request.setMerchantId(3001L);
        request.setAmount(12000L);
        request.setRequestedResult(PaymentRequestedResult.SUCCESS);
        return request;
    }
}
