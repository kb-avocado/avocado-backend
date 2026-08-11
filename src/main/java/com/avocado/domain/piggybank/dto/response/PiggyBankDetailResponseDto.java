package com.avocado.domain.piggybank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/** 저금통 상세 조회 응답 (목록 항목 + 남은 금액 + 보너스 정보) */
@Getter
@Builder
@AllArgsConstructor
public class PiggyBankDetailResponseDto {

    private final Long piggyBankId;
    private final String name;
    private final String icon;
    private final String status;
    private final Boolean favorite;
    private final Long savedAmount;      // 모은 금액 (balance)
    private final Long targetAmount;     // 목표 금액
    private final Integer progressRate;  // 달성률(%)
    private final Long remainingAmount;  // 남은 금액 (target - saved)
    private final String bonusType;      // NONE, FIXED, RATE
    private final Long bonusValue;       // 정액 금액 또는 비율
}