package com.avocado.domain.piggybank.domain;

public enum PiggyBankStatus {
    // 저금 진행 중
    ACTIVE,

    // 목표 금액은 달성했지만 유지 조건이 아직 남아 있는 상태
    PENDING_ACHIEVE,

    // 최종 목표 달성
    ACHIEVE,

    // 중도 해지
    CANCEL
}
