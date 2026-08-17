package com.avocado.domain.payment.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentQrTokenVo {

    private final Long userId;
    private final String token;
    private final long expiresIn;
}
