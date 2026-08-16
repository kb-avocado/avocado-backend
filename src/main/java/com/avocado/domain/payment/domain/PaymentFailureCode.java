package com.avocado.domain.payment.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentFailureCode {

    INVALID_OR_EXPIRED_QR("만료되었거나 올바르지 않은 QR 토큰"),

    FORCED_FAILURE("POS 시뮬레이터 임의 실패"),

    INSUFFICIENT_BALANCE("선불지갑 잔액 부족"),

    RESTRICTED_MERCHANT("아이 결제 제한 가맹점"),

    DAILY_LIMIT_EXCEEDED("일 한도 초과"),

    MONTHLY_LIMIT_EXCEEDED("월 한도 초과"),

    MERCHANT_NOT_AVAILABLE("이용할 수 없는 가맹점"),

    WALLET_NOT_AVAILABLE("이용할 수 없는 선불지갑");

    private final String description;
}
