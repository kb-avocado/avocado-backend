package com.avocado.domain.piggybank.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@Setter
public class PiggyBankDepositRequestDto {

    @NotNull
    @Positive
    private Long amount;
}