package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;

public interface PiggyBankBonusService {

    PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request);

    PiggyBankBonusPayResponseDto payBonus(Long piggyBankId);
}