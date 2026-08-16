package com.avocado.domain.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 계좌에서 발생하는 거래 유형을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum AccountTransactionType {

    WITHDRAWAL("계좌 출금"),

    REFUND("계좌 출금 환불");

    private final String description;
}
