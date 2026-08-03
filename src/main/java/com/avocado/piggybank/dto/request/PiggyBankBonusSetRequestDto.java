package com.avocado.piggybank.dto.request;

import com.avocado.piggybank.domain.BonusType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class PiggyBankBonusSetRequestDto {

    @NotNull(message = "보너스 타입은 필수입니다.")
    private BonusType bonusType;

    @NotNull(message = "보너스 값은 필수입니다.")
    @Positive(message = "보너스 값은 0보다 커야 합니다.")
    private Long bonusValue;
}