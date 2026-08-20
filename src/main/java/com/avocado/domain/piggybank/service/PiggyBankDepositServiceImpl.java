package com.avocado.domain.piggybank.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.transfer.service.TransferService;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.domain.PiggyBank;
import com.avocado.domain.piggybank.dto.request.PiggyBankDepositRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResultResponseDto;
import com.avocado.domain.piggybank.mapper.PiggyBankHistoryMapper;
import com.avocado.domain.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PiggyBankDepositServiceImpl implements PiggyBankDepositService {

    private final PiggyBankMapper piggyBankMapper;
    private final PiggyBankHistoryMapper piggyBankHistoryMapper;
    private final TransferService transferService;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Override
    public List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId, Long walletId) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);
        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return piggyBankHistoryMapper.selectDepositsByPiggyBankId(piggyBankId);
    }

    @Override
    @Transactional
    public PiggyBankDepositResultResponseDto deposit(
            Long piggyBankId,
            PiggyBankDepositRequestDto request
    ) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        if (!"ACTIVE".equals(piggyBank.getStatus())) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_ACTIVE);
        }

        Long remaining = piggyBank.getTargetAmount() - piggyBank.getBalance();
        if (request.getAmount() > remaining) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_DEPOSIT_EXCEEDS_TARGET);
        }

        Long balanceBefore = piggyBank.getBalance();
        Long balanceAfter = balanceBefore + request.getAmount();

        boolean goalReached = balanceAfter >= piggyBank.getTargetAmount();
        String newStatus = goalReached ? "PENDING_ACHIEVE" : "ACTIVE";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDepositedAt = piggyBank.getFirstDepositedAt() != null
                ? piggyBank.getFirstDepositedAt()
                : now;
        LocalDateTime targetReachedAt = goalReached ? now : piggyBank.getTargetReachedAt();

        piggyBankMapper.increaseBalance(piggyBankId, balanceAfter, newStatus, firstDepositedAt, targetReachedAt);

        String traceId = UUID.randomUUID().toString();
        piggyBankHistoryMapper.insertDeposit(piggyBankId, request.getAmount(), balanceBefore, balanceAfter, traceId);

        if (goalReached) {
            Long childId = piggyBankMapper.selectChildIdByPiggyBankId(piggyBankId);
            if (childId == null) {
                throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
            }

            notificationService.create(
                    childId,
                    NotificationType.PIGGY_BANK_ACHIEVED,
                    String.format("%s 저금통 목표 금액을 달성했습니다.", piggyBank.getName())
            );
        }

        return PiggyBankDepositResultResponseDto.builder()
                .piggyBankId(piggyBankId)
                .depositedAmount(request.getAmount())
                .balanceAfter(balanceAfter)
                .status(newStatus)
                .goalReached(goalReached)
                .depositedAt(now)
                .build();
    }

    @Override
    @Transactional
    public PiggyBankDepositResultResponseDto depositFromWallet(
            Long childId,
            Long piggyBankId,
            Long amount,
            String traceId,
            Long walletId
    ) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        // 소유권 검증: 이 저금통이 요청자의 지갑 소속인지
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        if (!"ACTIVE".equals(piggyBank.getStatus())) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_ACTIVE);
        }

        Long remaining = piggyBank.getTargetAmount() - piggyBank.getBalance();
        if (amount > remaining) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_DEPOSIT_EXCEEDS_TARGET);
        }

        // 지갑에서 저금할 금액 출금 — 같은 @Transactional 안에 묶여서
        // 아래 적립 로직이 실패하면 출금도 같이 롤백됨
        transferService.transferWalletToPiggyBank(childId, amount, traceId);

        Long balanceBefore = piggyBank.getBalance();
        Long balanceAfter = balanceBefore + amount;

        boolean goalReached = balanceAfter >= piggyBank.getTargetAmount();
        String newStatus = goalReached ? "PENDING_ACHIEVE" : "ACTIVE";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime firstDepositedAt = piggyBank.getFirstDepositedAt() != null
                ? piggyBank.getFirstDepositedAt()
                : now;
        LocalDateTime targetReachedAt = goalReached ? now : piggyBank.getTargetReachedAt();

        piggyBankMapper.increaseBalance(piggyBankId, balanceAfter, newStatus, firstDepositedAt, targetReachedAt);

        piggyBankHistoryMapper.insertDeposit(piggyBankId, amount, balanceBefore, balanceAfter, traceId);

        if (goalReached) {
            notificationService.create(
                    childId,
                    NotificationType.PIGGY_BANK_ACHIEVED,
                    String.format("%s 저금통 목표 금액을 달성했습니다.", piggyBank.getName())
            );
        }

        return PiggyBankDepositResultResponseDto.builder()
                .piggyBankId(piggyBankId)
                .depositedAmount(amount)
                .balanceAfter(balanceAfter)
                .status(newStatus)
                .goalReached(goalReached)
                .depositedAt(now)
                .build();
    }
}
