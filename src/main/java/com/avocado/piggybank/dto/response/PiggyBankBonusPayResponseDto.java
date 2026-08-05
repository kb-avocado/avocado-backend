package com.avocado.piggybank.dto.response;

import com.avocado.piggybank.domain.BonusType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PiggyBankBonusPayResponseDto {

    private final Long piggyBankId;
    private final BonusType bonusType;
    private final Long bonusValue;
    private final LocalDateTime paidAt;
}