package com.avocado.domain.payment.service;

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

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PaymentQrTokenRepository paymentQrTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${payment.qr-token.ttl-seconds:180}")
    private long paymentQrTokenTtlSeconds;

    @Override
    public PaymentQrTokenResponseDto issuePaymentQrToken(AuthUser authUser) {
        requireAuthenticated(authUser);

        return issuePaymentQrToken(authUser.getUserId());
    }

    @Override
    public PaymentQrTokenResponseDto reissuePaymentQrToken(AuthUser authUser) {
        requireAuthenticated(authUser);

        paymentQrTokenRepository.deleteByUserId(authUser.getUserId());

        return issuePaymentQrToken(authUser.getUserId());
    }

    private PaymentQrTokenResponseDto issuePaymentQrToken(Long userId) {
        String token = generateToken();

        paymentQrTokenRepository.save(
                userId,
                token,
                paymentQrTokenTtl()
        );

        return PaymentQrTokenResponseDto.builder()
                .token(token)
                .expiresIn(paymentQrTokenTtlSeconds)
                .build();
    }

    private void requireAuthenticated(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
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
}
