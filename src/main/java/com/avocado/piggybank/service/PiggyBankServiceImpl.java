package com.avocado.piggybank.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.piggybank.domain.PiggyBank;

import com.avocado.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankResponseDto;
import com.avocado.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.piggybank.dto.request.PiggyBankCreateRequestDto;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 저금통 목록 조회
public class PiggyBankServiceImpl implements PiggyBankService {
    // 저금 목표는 최대 3개까지
    private static final int MAX_COUNT = 3;

    private final PiggyBankMapper piggyBankMapper;

    @Override
    public PiggyBankListResponseDto getList(Long walletId, String status) {
        // 탭(IN_PROGRESS/CLOSED) → 실제 DB status 값들로 변환
        List<String> statuses = toStatuses(status);

        // 조회 후 도메인 → 응답 DTO 변환
        List<PiggyBankResponseDto> piggyBanks = piggyBankMapper
                .selectByWalletIdAndStatuses(walletId, statuses)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        // 진행중 개수는 탭과 무관하게 항상 계산 (생성 가능 여부 판단용)
        int activeCount = piggyBankMapper.countByWalletIdAndStatuses(
                walletId, List.of("ACTIVE", "PENDING_ACHIEVE"));

        return PiggyBankListResponseDto.builder()
                .piggyBanks(piggyBanks)
                .activeCount(activeCount)
                .maxCount(MAX_COUNT)
                .canCreate(activeCount < MAX_COUNT)
                .build();
    }

    /**
     * 화면 탭을 실제 저장 상태값으로 매핑.
     *  - CLOSED      : ACHIEVE(달성), CANCEL(취소)
     *  - IN_PROGRESS : ACTIVE(진행중), PENDING_ACHIEVE(7일 대기중)
     */
    private List<String> toStatuses(String group) {
        if ("CLOSED".equalsIgnoreCase(group)) {
            return List.of("ACHIEVE", "CANCEL");        // 목록 조회 완료 탭
        }
        return List.of("ACTIVE", "PENDING_ACHIEVE");     // 목록 조회 진행중인 탭
    }

    // 도메인 → 응답 DTO. progressRate(달성률)는 여기서 계산
    private PiggyBankResponseDto toDto(PiggyBank p) {
        long target = p.getTargetAmount() == null ? 0 : p.getTargetAmount();
        long saved = p.getBalance() == null ? 0 : p.getBalance();
        int rate = target == 0 ? 0 : (int) (saved * 100 / target);

        return PiggyBankResponseDto.builder()
                .piggyBankId(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .favorite(p.getIsFavorite())
                .savedAmount(saved)
                .targetAmount(target)
                .progressRate(rate)
                .build();
    }

    // 저금통 상세 조회
    @Override
    public PiggyBankDetailResponseDto getDetail(Long piggyBankId) {
        PiggyBank p = piggyBankMapper.selectById(piggyBankId);
        // 팀 공용 예외 처리
        if (p == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        long target = p.getTargetAmount() == null ? 0 : p.getTargetAmount();
        long saved = p.getBalance() == null ? 0 : p.getBalance();
        int rate = target == 0 ? 0 : (int) (saved * 100 / target);
        long remaining = Math.max(0, target - saved);

        return PiggyBankDetailResponseDto.builder()
                .piggyBankId(p.getId())
                .name(p.getName())
                .status(p.getStatus())
                .favorite(p.getIsFavorite())
                .savedAmount(saved)
                .targetAmount(target)
                .progressRate(rate)
                .remainingAmount(remaining)
                .bonusType(p.getBonusType() == null ? "NONE" : p.getBonusType().name())
                .bonusValue(p.getBonusValue())
                .build();
    }
    @Override
    @Transactional
    public PiggyBankDetailResponseDto create(Long walletId, PiggyBankCreateRequestDto request) {
        // 1) 최대 개수(3) 체크 — 진행중 개수로 판단
        int activeCount = piggyBankMapper.countByWalletIdAndStatuses(
                walletId, List.of("ACTIVE", "PENDING_ACHIEVE"));
        if (activeCount >= MAX_COUNT) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_LIMIT_EXCEEDED);
        }

        // 2) 저금통 생성 (나머지 컬럼은 DB 기본값)
        PiggyBank piggyBank = PiggyBank.builder()
                .walletId(walletId)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .build();
        piggyBankMapper.insert(piggyBank);

        // 3) 생성된 id로 상세 반환
        Long newId = piggyBankMapper.selectLastInsertId();
        return getDetail(newId);
    }
}