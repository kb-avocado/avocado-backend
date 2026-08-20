package com.avocado.domain.piggybank.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.piggybank.domain.PiggyBankBonusReminderTarget;
import com.avocado.domain.piggybank.domain.PiggyBankBonusType;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.domain.PiggyBank;
import com.avocado.domain.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.domain.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankBonusServiceImpl implements PiggyBankBonusService {

    private final PiggyBankMapper piggyBankMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PiggyBankBonusResponseDto setBonus(Long piggyBankId, PiggyBankBonusSetRequestDto request, Long walletId, Long childId) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        // 소유권 검증: 이 저금통이 요청자의 지갑 소속인지
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (piggyBank.getBonusType() != PiggyBankBonusType.NONE) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_BONUS_ALREADY_SET);
        }

        validateBonusValue(request.getPiggyBankBonusType(), request.getBonusValue());

        piggyBankMapper.updateBonus(piggyBankId, request.getPiggyBankBonusType(), request.getBonusValue());

        notificationService.create(
                childId,
                NotificationType.PIGGY_BANK_BONUS_SET,
                String.format("%s 저금통에 보너스가 설정됐어요.", piggyBank.getName())
        );

        return PiggyBankBonusResponseDto.builder()
                .piggyBankId(piggyBankId)
                .piggyBankBonusType(request.getPiggyBankBonusType())
                .bonusValue(request.getBonusValue())
                .build();
    }

    @Override
    @Transactional
    public PiggyBankBonusPayResponseDto payBonus(Long piggyBankId, Long walletId) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }
      
        // 소유권 검증: 이 저금통이 요청자의 지갑 소속인지
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (piggyBank.getBonusType() == PiggyBankBonusType.NONE) {
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
                .piggyBankBonusType(piggyBank.getBonusType())
                .bonusValue(piggyBank.getBonusValue())
                .paidAt(paidAt)
                .build();
    }

    @Override
    @Transactional
    public int sendBonusReminders() {
        List<PiggyBankBonusReminderTarget> targets = piggyBankMapper.selectBonusReminderTargets();

        for (PiggyBankBonusReminderTarget target : targets) {
            notificationService.create(
                    target.getParentId(),
                    NotificationType.PIGGY_BANK_BONUS_REMINDER,
                    String.format("%s 저금통 보너스 지급을 기다리고 있어요.", target.getName())
            );
        }

        return targets.size();
    }

    private void validateBonusValue(PiggyBankBonusType piggyBankBonusType, Long bonusValue) {
        if (piggyBankBonusType == PiggyBankBonusType.RATE && (bonusValue < 1 || bonusValue > 100)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (piggyBankBonusType == PiggyBankBonusType.FIXED && bonusValue <= 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (piggyBankBonusType == PiggyBankBonusType.NONE && bonusValue != 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}