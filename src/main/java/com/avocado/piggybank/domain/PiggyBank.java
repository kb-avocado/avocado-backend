package com.avocado.piggybank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PiggyBank {

    private final Long id;
    private final Long walletId;
    private final String name;
    private final Long targetAmount;
    private final Long balance;
    private final BonusType bonusType;
    private final Long bonusValue;
    private final String status;
    private final Boolean isFavorite;
    private final LocalDateTime firstDepositedAt;
    private final LocalDateTime targetReachedAt;
    private final LocalDateTime achievedAt;
    private final LocalDateTime bonusPaidAt;
    private final LocalDateTime canceledAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}