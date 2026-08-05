package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;

import java.util.List;

public interface PiggyBankDepositService {

    List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId);
}