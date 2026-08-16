package com.avocado.domain.payment.scheduler;

import com.avocado.domain.payment.repository.PaymentQrTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentQrTokenCleanupScheduler {

    private final PaymentQrTokenRepository paymentQrTokenRepository;

    @Scheduled(fixedDelayString = "${payment.qr-token.cleanup-delay-millis:60000}")
    public void cleanupExpiredTokens() {
        paymentQrTokenRepository.cleanupExpiredTokens(System.currentTimeMillis());
    }
}
