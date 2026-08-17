package com.avocado.domain.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentRequestedResult {

    SUCCESS("결제 성공 시뮬레이션"),

    FORCE_FAIL("결제 실패 시뮬레이션");

    private final String description;
}
