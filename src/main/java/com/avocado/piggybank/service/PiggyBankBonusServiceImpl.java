package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;
import org.springframework.stereotype.Service;

@Service
public class PiggyBankBonusServiceImpl implements PiggyBankBonusService {

    @Override
    public PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request) {
        // TODO: Mapper 연동되면 (1) 저금통 존재 확인 (2) 이미 설정됐는지(PGB-013) 확인 (3) DB 업데이트로 교체
        return PiggyBankBonusResponseDto.builder()
                .piggyBankId(piggyBankId)
                .bonusType(request.getBonusType())
                .bonusValue(request.getBonusValue())
                .build();
    }
}