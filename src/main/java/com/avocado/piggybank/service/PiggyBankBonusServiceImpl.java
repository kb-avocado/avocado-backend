package com.avocado.piggybank.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.response.code.ErrorCode;
import com.avocado.piggybank.domain.BonusType;
import com.avocado.piggybank.domain.PiggyBank;
import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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

    @Override
    @Transactional
    public PiggyBankBonusPayResponseDto payBonus(Long piggyBankId) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        if (piggyBank.getBonusType() == BonusType.NONE) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_BONUS_NOT_SET);
        }

        if (!"ACHIEVE".equals(piggyBank.getStatus())) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_GOAL_NOT_ACHIEVED);
        }

        if (piggyBank.getBonusPaidAt() != null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_BONUS_ALREADY_PAID);
        }

        // TODO: 실제 송금(부모 계좌 → 아이 지갑)은 지갑 담당자 로직과 연동 필요, 이 API는 "지급 완료 표시"만 담당
        LocalDateTime paidAt = LocalDateTime.now();
        piggyBankMapper.markBonusPaid(piggyBankId, paidAt);

        return PiggyBankBonusPayResponseDto.builder()
                .piggyBankId(piggyBankId)
                .bonusType(piggyBank.getBonusType())
                .bonusValue(piggyBank.getBonusValue())
                .paidAt(paidAt)
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