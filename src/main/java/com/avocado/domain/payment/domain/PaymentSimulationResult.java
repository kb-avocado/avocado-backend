package com.avocado.domain.payment.domain;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentSimulationResult {

    private final Long walletHistoryId;

    private final Long merchantId;

    private final String merchantName;

    private final Long amount;

    private final String status;

    private final PaymentFailureCode failureCode;

    private final Long balanceAfter;

    public static PaymentSimulationResult invalidOrExpiredQr(
            Long merchantId,
            Long amount
    ) {
        return PaymentSimulationResult.builder()
                .merchantId(merchantId)
                .amount(amount)
                .status("FAILED")
                .failureCode(PaymentFailureCode.INVALID_OR_EXPIRED_QR)
                .build();
    }
}
