package com.avocado.domain.payment.service;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    private static final long TOKEN_TTL_SECONDS = 180L;

    @Mock
    private PaymentQrTokenRepository paymentQrTokenRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentQrTokenRepository);
        ReflectionTestUtils.setField(
                paymentService,
                "paymentQrTokenTtlSeconds",
                TOKEN_TTL_SECONDS
        );
    }

    @Test
    @DisplayName("로그인 사용자 기준 결제 QR 토큰을 발급하고 Redis에 TTL과 함께 저장한다")
    void issuePaymentQrToken_success() {
        // given
        AuthUser authUser = authUser(102L);
        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);

        // when
        PaymentQrTokenResponseDto result = paymentService.issuePaymentQrToken(authUser);

        // then
        assertThat(result.getToken()).isNotBlank();
        assertThat(result.getExpiresIn()).isEqualTo(TOKEN_TTL_SECONDS);

        verify(paymentQrTokenRepository).save(
                eq(authUser.getUserId()),
                tokenCaptor.capture(),
                eq(Duration.ofSeconds(TOKEN_TTL_SECONDS))
        );
        assertThat(tokenCaptor.getValue()).isEqualTo(result.getToken());
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

        // when
        PaymentQrTokenResponseDto result = paymentService.reissuePaymentQrToken(authUser);

        // then
        verify(paymentQrTokenRepository).deleteByUserId(authUser.getUserId());
        verify(paymentQrTokenRepository).save(
                eq(authUser.getUserId()),
                tokenCaptor.capture(),
                eq(Duration.ofSeconds(TOKEN_TTL_SECONDS))
        );
        assertThat(tokenCaptor.getValue()).isEqualTo(result.getToken());
    }

    private AuthUser authUser(Long userId) {
        return AuthUser.builder()
                .userId(userId)
                .role(UserRole.USER)
                .userType(UserType.CHILD)
                .build();
    }
}
