package com.avocado.domain.piggybank.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 저금통 목표 달성 보상의 계산 방식을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum PiggyBankBonusType {

    NONE("보너스 없음"),

    FIXED("고정 금액 보너스"),

    RATE("비율 기반 보너스");

    private final String description;
}