package com.avocado.domain.piggybank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

// 자동환급 대상 저금통(7일 경과) 한 건을 나타낸다.
@Getter
@Builder
@AllArgsConstructor
public class PiggyBankRefundTarget {
    private final Long piggyBankId;
    private final Long childId;
    private final Long balance;
}