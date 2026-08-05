package com.avocado.piggybank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PiggyBankDepositResponseDto {

    private final Long depositId;
    private final Long amount;
    private final Long balanceAfter;
    private final LocalDateTime depositedAt;
}