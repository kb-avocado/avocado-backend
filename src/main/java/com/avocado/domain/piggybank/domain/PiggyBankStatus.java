package com.avocado.domain.piggybank.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 저금통의 진행 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum PiggyBankStatus {

    ACTIVE("저축 진행 중"),

    PENDING_ACHIEVE("목표 금액 달성 후 최종 달성 대기"),

    ACHIEVE("최종 목표 달성"),

    CANCEL("저금통 해지");

    private final String description;
}
