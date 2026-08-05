package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.request.PiggyBankDepositRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankDepositResultResponseDto;

import java.util.List;

public interface PiggyBankDepositService {

    List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId);

    PiggyBankDepositResultResponseDto deposit(Long piggyBankId, PiggyBankDepositRequestDto request);
}