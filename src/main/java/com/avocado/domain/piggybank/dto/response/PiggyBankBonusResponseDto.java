package com.avocado.domain.piggybank.dto.response;

import com.avocado.domain.piggybank.domain.BonusType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PiggyBankBonusResponseDto {

    private final Long piggyBankId;
    private final BonusType bonusType;
    private final Long bonusValue;
}