package com.avocado.piggybank.dto.response;

import com.avocado.piggybank.domain.BonusType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PiggyBankBonusResponseDto {

    private final Long piggyBankId;
    private final BonusType bonusType;
    private final Long bonusValue;
}