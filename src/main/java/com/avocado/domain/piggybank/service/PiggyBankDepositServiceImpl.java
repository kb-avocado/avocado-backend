package com.avocado.domain.piggybank.service;

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

    @Override
    public List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId) {
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

        // TODO: 지갑 잔액 검증 및 차감 (WalletService 준비되면 연결)
        // walletService.deductBalance(piggyBank.getWalletId(), request.getAmount());

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
            String traceId
    ) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);

        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        if (!"ACTIVE".equals(piggyBank.getStatus())) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_ACTIVE);
        }

        Long remaining = piggyBank.getTargetAmount() - piggyBank.getBalance();
        if (amount > remaining) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_DEPOSIT_EXCEEDS_TARGET);
        }

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