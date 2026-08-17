package com.avocado.domain.piggybank.dto.response;

import com.avocado.domain.piggybank.domain.PiggyBankBonusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PiggyBankBonusPayResponseDto {

    private final Long piggyBankId;
    private final PiggyBankBonusType piggyBankBonusType;
    private final Long bonusValue;
    private final LocalDateTime paidAt;
}