package com.avocado.domain.piggybank.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PiggyBank {

    // 저금통 PK
    private final Long id;

    // 저금통을 소유한 아이의 선불지갑 PK
    private final Long walletId;

    // 저금통 이름
    private final String name;

    // 저금통 아이콘
    private final String icon;

    // 목표 금액
    private final Long targetAmount;

    // 현재 저금통에 적립되어 있는 금액
    private final Long balance;

    // 보너스 지급 방식
    // NONE, FIXED, RATE
    private final PiggyBankBonusType bonusType;

    // 보너스 값
    // FIXED이면 금액, RATE이면 비율
    private final Long bonusValue;

    //저금통 상태, 즐겨찾기 여부 에러때문에 임시로 자리 바꿔놓음
    // 저금통 상태
    // ACTIVE, PENDING_ACHIEVE, ACHIEVE, CANCEL
    private final String status;

    // 즐겨찾기 여부
    private final Boolean isFavorite;

    // 최초 입금 일시
    private final LocalDateTime firstDepositedAt;

    // 목표 금액 최초 도달 일시
    private final LocalDateTime targetReachedAt;

    // 최종 목표 달성 일시
    private final LocalDateTime achievedAt;

    // 목표 달성 보너스 실제 지급 일시
    private final LocalDateTime bonusPaidAt;

    // 중도 해지 일시
    private final LocalDateTime canceledAt;

    // 저금통 생성 일시
    private final LocalDateTime createdAt;

    // 저금통 수정 일시
    private final LocalDateTime updatedAt;
}