package com.avocado.domain.piggybank.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PiggyBankDepositResultResponseDto {

    private final Long piggyBankId;
    private final Long depositedAmount;
    private final Long balanceAfter;
    private final String status;
    private final boolean goalReached;
    private final LocalDateTime depositedAt;
}