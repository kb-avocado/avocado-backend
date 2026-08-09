package com.avocado.domain.piggybank.service;

import com.avocado.domain.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusResponseDto;

public interface PiggyBankBonusService {

    PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request);

    PiggyBankBonusPayResponseDto payBonus(Long piggyBankId);
}