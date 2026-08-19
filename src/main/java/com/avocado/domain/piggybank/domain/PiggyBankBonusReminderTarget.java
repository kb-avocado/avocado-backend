package com.avocado.domain.piggybank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 목표를 달성했지만 보너스를 아직 지급하지 않은 저금통(재촉 알림 대상) 한 건을 나타낸다.
@Getter
@Builder
@AllArgsConstructor
public class PiggyBankBonusReminderTarget {
    private final Long piggyBankId;
    private final Long parentId;
    private final String name;
}