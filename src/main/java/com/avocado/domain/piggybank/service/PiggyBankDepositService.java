package com.avocado.domain.piggybank.service;

import com.avocado.domain.piggybank.dto.request.PiggyBankDepositRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResultResponseDto;

import java.util.List;

public interface PiggyBankDepositService {

    List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId, Long walletId);

    PiggyBankDepositResultResponseDto deposit(Long piggyBankId, PiggyBankDepositRequestDto request);

    PiggyBankDepositResultResponseDto depositFromWallet(
            Long childId,
            Long piggyBankId,
            Long amount,
            String traceId,
            Long walletId
    );
}