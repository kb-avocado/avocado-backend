package com.avocado.domain.piggybank.dto.response;

import com.avocado.domain.piggybank.domain.PiggyBankBonusType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PiggyBankBonusResponseDto {

    private final Long piggyBankId;
    private final PiggyBankBonusType piggyBankBonusType;
    private final Long bonusValue;
}