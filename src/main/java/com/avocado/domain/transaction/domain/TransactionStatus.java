package com.avocado.domain.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 금융 거래의 처리 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum TransactionStatus {

    PENDING("거래 처리 중"),

    SUCCESS("거래 처리 성공"),

    FAILED("거래 처리 실패");

    private final String description;
}
