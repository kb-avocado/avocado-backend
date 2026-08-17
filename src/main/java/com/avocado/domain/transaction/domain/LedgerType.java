package com.avocado.domain.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 원장의 금액 증감 방향을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum LedgerType {

    IN("잔액 증가"),

    OUT("잔액 감소");

    private final String description;
}
