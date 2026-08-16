package com.avocado.domain.payment.service;

import com.avocado.domain.payment.domain.PaymentQrTokenVo;
import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PaymentQrTokenRepository paymentQrTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${payment.qr-token.ttl-seconds:180}")
    private long paymentQrTokenTtlSeconds;

    @Value("${payment.qr-token.reissue-lock-seconds:3}")
    private long paymentQrReissueLockSeconds;

    @Override
    public PaymentQrTokenResponseDto issuePaymentQrToken(AuthUser authUser) {
        requireAuthenticated(authUser);

        return PaymentQrTokenResponseDto.from(
                issuePaymentQrToken(authUser.getUserId())
        );
    }

    @Override
    public PaymentQrTokenResponseDto reissuePaymentQrToken(AuthUser authUser) {
        requireAuthenticated(authUser);
        validateReissueAllowed(authUser.getUserId());

        return PaymentQrTokenResponseDto.from(
                issuePaymentQrToken(authUser.getUserId())
        );
    }

    @Override
    public List<PaymentQrActiveTokenResponseDto> getActivePaymentQrTokens() {
        long nowMillis = System.currentTimeMillis();

        // POS 목록은 항상 조회 시점 기준으로 만료 토큰을 먼저 걷어낸 뒤 만든다.
        paymentQrTokenRepository.cleanupExpiredTokens(nowMillis);

        return paymentQrTokenRepository.findActiveTokens(nowMillis).stream()
                .map(PaymentQrActiveTokenResponseDto::from)
                .toList();
    }

    private PaymentQrTokenVo issuePaymentQrToken(Long userId) {
        String token = generateToken();

        // 사용자별 QR은 하나만 활성화한다. 재발급과 신규 발급 모두 기존 토큰을 먼저 무효화한다.
        paymentQrTokenRepository.deleteByUserId(userId);
        paymentQrTokenRepository.save(
                userId,
                token,
                paymentQrTokenTtl(),
                paymentQrTokenExpiresAtMillis()
        );

        return PaymentQrTokenVo.builder()
                .userId(userId)
                .token(token)
                .expiresIn(paymentQrTokenTtlSeconds)
                .build();
    }

    private void requireAuthenticated(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private void validateReissueAllowed(Long userId) {
        boolean acquired = paymentQrTokenRepository.acquireReissueLock(
                userId,
                paymentQrReissueLockTtl()
        );

        if (!acquired) {
            throw new BusinessException(ErrorCode.PAYMENT_QR_REISSUE_TOO_FREQUENT);
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_BYTE_LENGTH];
        secureRandom.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private Duration paymentQrTokenTtl() {
        return Duration.ofSeconds(paymentQrTokenTtlSeconds);
    }

    private long paymentQrTokenExpiresAtMillis() {
        return System.currentTimeMillis() + paymentQrTokenTtl().toMillis();
    }

    private Duration paymentQrReissueLockTtl() {
        return Duration.ofSeconds(paymentQrReissueLockSeconds);
    }
}
