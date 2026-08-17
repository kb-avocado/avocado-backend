package com.avocado.domain.payment.domain;
import com.avocado.global.response.code.ErrorCode;
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

    private final ErrorCode failureCode;

    private final Long balanceAfter;

    public static PaymentSimulationResult invalidOrExpiredQr(
            Long merchantId,
            Long amount
    ) {
        return PaymentSimulationResult.builder()
                .merchantId(merchantId)
                .amount(amount)
                .status("FAILED")
                .failureCode(ErrorCode.INVALID_OR_EXPIRED_QR)
                .build();
    }
}
