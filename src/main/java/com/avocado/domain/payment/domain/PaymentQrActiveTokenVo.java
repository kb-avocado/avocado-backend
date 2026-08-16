package com.avocado.domain.payment.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentQrActiveTokenVo {

    private final String token;
    private final Long userId;
    private final long expiresIn;
}
