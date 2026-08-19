package com.avocado.domain.piggybank.service;

import com.avocado.domain.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusResponseDto;

public interface PiggyBankBonusService {

    PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request, Long walletId, Long childId);

    PiggyBankBonusPayResponseDto payBonus(Long piggyBankId, Long walletId);

    // 보너스 미지급 저금통에 재촉 알림 발송 (스케줄러가 호출), 발송 건수 반환
    int sendBonusReminders();
}