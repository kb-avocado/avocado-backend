package com.avocado.domain.payment.service;

import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final Duration PAYMENT_QR_TOKEN_TTL = Duration.ofMinutes(3);
    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PaymentQrTokenRepository paymentQrTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public PaymentQrTokenResponseDto issuePaymentQrToken(AuthUser authUser) {
        requireAuthenticated(authUser);

        String token = generateToken();

        paymentQrTokenRepository.save(
                authUser.getUserId(),
                token,
                PAYMENT_QR_TOKEN_TTL
        );

        return PaymentQrTokenResponseDto.builder()
                .token(token)
                .expiresIn(PAYMENT_QR_TOKEN_TTL.toSeconds())
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
}
