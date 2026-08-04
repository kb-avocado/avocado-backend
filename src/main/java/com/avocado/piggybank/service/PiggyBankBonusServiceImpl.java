package com.avocado.piggybank.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.piggybank.domain.BonusType;
import com.avocado.piggybank.domain.PiggyBank;
import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PiggyBankBonusServiceImpl implements PiggyBankBonusService {

    private final PiggyBankMapper piggyBankMapper;

    @Override
    public PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        if (piggyBank.getBonusType() != BonusType.NONE) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_BONUS_ALREADY_SET);
        }

        validateBonusValue(request.getBonusType(), request.getBonusValue());

        piggyBankMapper.updateBonus(piggyBankId, request.getBonusType(), request.getBonusValue());

        return PiggyBankBonusResponseDto.builder()
                .piggyBankId(piggyBankId)
                .bonusType(request.getBonusType())
                .bonusValue(request.getBonusValue())
                .build();
    }

    private void validateBonusValue(BonusType bonusType, Long bonusValue) {
        if (bonusType == BonusType.RATE && (bonusValue < 1 || bonusValue > 100)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (bonusType == BonusType.FIXED && bonusValue <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}