package com.avocado.domain.piggybank.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 저금통에서 발생하는 금액 이동 유형을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum PiggyBankHistoryType {

    DEPOSIT("저금통 입금"),

    WITHDRAWAL("저금통 출금");

    private final String description;
}