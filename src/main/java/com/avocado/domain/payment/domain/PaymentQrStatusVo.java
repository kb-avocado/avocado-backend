package com.avocado.domain.payment.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentQrStatusVo {

    private final PaymentQrStatus status;
    private final Long userId;
    private final Long expiresAtMillis;
    private final Long expiresIn;
    private final Long walletHistoryId;
    private final Long merchantId;
    private final String merchantName;
    private final Long amount;
    private final String failureCode;
    private final Long balanceAfter;

    public static PaymentQrStatusVo invalid() {
        return PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.INVALID)
                .expiresIn(0L)
                .build();
    }

    public static PaymentQrStatusVo expired() {
        return PaymentQrStatusVo.builder()
                .status(PaymentQrStatus.EXPIRED)
                .expiresIn(0L)
                .build();
    }
}
