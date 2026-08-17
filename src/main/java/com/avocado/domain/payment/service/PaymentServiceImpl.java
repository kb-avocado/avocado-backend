package com.avocado.domain.payment.service;

import com.avocado.domain.payment.domain.PaymentQrTokenVo;
import com.avocado.domain.payment.domain.PaymentQrStatus;
import com.avocado.domain.payment.domain.PaymentQrStatusVo;
import com.avocado.domain.payment.domain.PaymentSimulationResult;
import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrStatusResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import com.avocado.domain.wallet.service.WalletService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final int TOKEN_BYTE_LENGTH = 32;

    private final PaymentQrTokenRepository paymentQrTokenRepository;
    private final WalletService walletService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${payment.qr-token.ttl-seconds:180}")
    private long paymentQrTokenTtlSeconds;

    @Value("${payment.qr-token.reissue-lock-seconds:3}")
    private long paymentQrReissueLockSeconds;

    @Value("${payment.qr-token.status-retention-seconds:60}")
    private long paymentQrStatusRetentionSeconds;

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
    public PaymentQrStatusResponseDto getPaymentQrStatus(
            AuthUser authUser,
            String qrToken
    ) {
        requireAuthenticated(authUser);
        validateQrToken(qrToken);

        long nowMillis = System.currentTimeMillis();

        return PaymentQrStatusResponseDto.from(
                resolvePaymentQrStatus(
                        authUser.getUserId(),
                        qrToken,
                        nowMillis
                )
        );
    }

    @Override
    public PaymentSimulationResponseDto simulatePayment(
            PaymentSimulationRequestDto request
    ) {
        Optional<Long> userId = paymentQrTokenRepository.consumeUserIdByToken(
                request.getQrToken()
        );

        if (userId.isEmpty()) {
            return PaymentSimulationResponseDto.from(
                    PaymentSimulationResult.invalidOrExpiredQr(
                            request.getMerchantId(),
                            request.getAmount()
                    )
            );
        }

        PaymentSimulationResult result = walletService.processPosPayment(
                userId.get(),
                request.getMerchantId(),
                request.getAmount(),
                request.getRequestedResult()
        );

        paymentQrTokenRepository.saveCompletedStatus(
                request.getQrToken(),
                userId.get(),
                result,
                paymentQrStatusRetentionTtl()
        );

        return PaymentSimulationResponseDto.from(result);
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
        Optional<String> previousToken = paymentQrTokenRepository.findTokenByUserId(userId);
        Duration tokenTtl = paymentQrTokenTtl();
        long expiresAtMillis = System.currentTimeMillis() + tokenTtl.toMillis();

        // 사용자별 QR은 하나만 활성화한다. 재발급과 신규 발급 모두 기존 토큰을 먼저 무효화한다.
        paymentQrTokenRepository.deleteByUserId(userId);
        previousToken.ifPresent(expiredToken -> paymentQrTokenRepository.saveExpiredStatus(
                expiredToken,
                userId,
                paymentQrStatusRetentionTtl()
        ));
        paymentQrTokenRepository.save(
                userId,
                token,
                tokenTtl,
                expiresAtMillis
        );
        paymentQrTokenRepository.saveWaitingStatus(
                userId,
                token,
                tokenTtl.plus(paymentQrStatusRetentionTtl()),
                expiresAtMillis
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

    private void validateQrToken(String qrToken) {
        if (qrToken == null || qrToken.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private PaymentQrStatusVo resolvePaymentQrStatus(
            Long userId,
            String qrToken,
            long nowMillis
    ) {
        Optional<PaymentQrStatusVo> savedStatus = paymentQrTokenRepository.findStatusByToken(qrToken);

        if (savedStatus.isPresent()) {
            return resolveSavedPaymentQrStatus(
                    userId,
                    savedStatus.get(),
                    nowMillis
            );
        }

        return resolveActivePaymentQrStatusWithoutSavedStatus(
                userId,
                qrToken
        );
    }

    private PaymentQrStatusVo resolveSavedPaymentQrStatus(
            Long userId,
            PaymentQrStatusVo savedStatus,
            long nowMillis
    ) {
        if (!userId.equals(savedStatus.getUserId())) {
            return PaymentQrStatusVo.invalid();
        }

        if (PaymentQrStatus.WAITING.equals(savedStatus.getStatus())) {
            long expiresIn = remainingSeconds(
                    savedStatus.getExpiresAtMillis(),
                    nowMillis
            );

            if (expiresIn <= 0L) {
                return PaymentQrStatusVo.expired();
            }

            return PaymentQrStatusVo.builder()
                    .status(PaymentQrStatus.WAITING)
                    .userId(userId)
                    .expiresAtMillis(savedStatus.getExpiresAtMillis())
                    .expiresIn(expiresIn)
                    .build();
        }

        return savedStatus;
    }

    private PaymentQrStatusVo resolveActivePaymentQrStatusWithoutSavedStatus(
            Long userId,
            String qrToken
    ) {
        Optional<Long> tokenUserId = paymentQrTokenRepository.findUserIdByToken(qrToken);

        if (tokenUserId.isEmpty() || !userId.equals(tokenUserId.get())) {
            return PaymentQrStatusVo.invalid();
        }

        long expiresIn = paymentQrTokenRepository.getTokenTtlSeconds(qrToken);

        if (expiresIn <= 0L) {
            return PaymentQrStatusVo.expired();
        }

        return PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.WAITING)
                .userId(userId)
                .expiresIn(expiresIn)
                .build();
    }

    private long remainingSeconds(
            Long expiresAtMillis,
            long nowMillis
    ) {
        if (expiresAtMillis == null) {
            return 0L;
        }

        long remainingMillis = Math.max(0L, expiresAtMillis - nowMillis);

        return (remainingMillis + 999L) / 1000L;
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

    private Duration paymentQrReissueLockTtl() {
        return Duration.ofSeconds(paymentQrReissueLockSeconds);
    }

    private Duration paymentQrStatusRetentionTtl() {
        return Duration.ofSeconds(paymentQrStatusRetentionSeconds);
    }
}
