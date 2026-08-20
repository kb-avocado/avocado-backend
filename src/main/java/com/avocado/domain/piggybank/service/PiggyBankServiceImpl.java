package com.avocado.domain.piggybank.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.piggybank.domain.PiggyBankBonusType;
import com.avocado.domain.piggybank.domain.PiggyBankRefundTarget;
import com.avocado.domain.piggybank.mapper.PiggyBankHistoryMapper;
import com.avocado.domain.transfer.service.TransferService;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.domain.PiggyBank;

import com.avocado.domain.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankResponseDto;
import com.avocado.domain.piggybank.mapper.PiggyBankMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.avocado.domain.piggybank.dto.request.PiggyBankCreateRequestDto;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
// 저금통 목록 조회
public class PiggyBankServiceImpl implements PiggyBankService {
    // 저금 목표는 최대 3개까지
    private static final int MAX_COUNT = 3;

    private final PiggyBankMapper piggyBankMapper;
    private final TransferService transferService;
    private final PiggyBankHistoryMapper piggyBankHistoryMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Override
    public PiggyBankListResponseDto getList(Long walletId, String status) {
        // 탭(IN_PROGRESS/BONUS_UNPAID/CLOSED) → 실제 조회 방식으로 매핑
        List<PiggyBankResponseDto> piggyBanks = fetchByGroup(walletId, status)
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
     * - IN_PROGRESS  : ACTIVE(진행중), PENDING_ACHIEVE(7일 대기중)
     * - BONUS_UNPAID : ACHIEVE(달성) 중 보너스가 설정돼 있고 아직 미지급
     * - CLOSED       : ACHIEVE(달성) 중 보너스가 없거나(NONE) 이미 지급 완료
     */
    private List<PiggyBank> fetchByGroup(Long walletId, String group) {
        if ("BONUS_UNPAID".equalsIgnoreCase(group)) {
            return piggyBankMapper.selectBonusUnpaidByWalletId(walletId);
        }
        if ("CLOSED".equalsIgnoreCase(group)) {
            return piggyBankMapper.selectCompletedByWalletId(walletId);
        }
        return piggyBankMapper.selectByWalletIdAndStatuses(walletId, List.of("ACTIVE", "PENDING_ACHIEVE"));
    }

    // 도메인 → 응답 DTO. progressRate(달성률)는 여기서 계산
    private PiggyBankResponseDto toDto(PiggyBank p) {
        long target = p.getTargetAmount() == null ? 0 : p.getTargetAmount();
        long saved = p.getBalance() == null ? 0 : p.getBalance();
        int rate = target == 0 ? 0 : (int) (saved * 100 / target);

        return PiggyBankResponseDto.builder()
                .piggyBankId(p.getId())
                .name(p.getName())
                .icon(p.getIcon())
                .status(p.getStatus())
                .favorite(p.getIsFavorite())
                .savedAmount(saved)
                .targetAmount(target)
                .progressRate(rate)
                .firstDepositedAt(p.getFirstDepositedAt())
                .bonus(toBonusDto(p)) // 추가
                .build();
    }

    // 저금통 목록 조회 보너스 정보 포함
    // 도메인의 보너스 필드 → bonus DTO
    private PiggyBankResponseDto.BonusDto toBonusDto(PiggyBank p) {
        PiggyBankBonusType type = p.getBonusType();
        if (type == null || type == PiggyBankBonusType.NONE) {
            return PiggyBankResponseDto.BonusDto.builder()
                    .status("UNPAID").type("NONE").build();
        }
        String status = (p.getBonusPaidAt() != null) ? "PAID" : "UNPAID";
        return PiggyBankResponseDto.BonusDto.builder()
                .status(status)
                .type(type.name())
                .amount(type == PiggyBankBonusType.FIXED ? p.getBonusValue() : null)
                .rate(type == PiggyBankBonusType.RATE ? p.getBonusValue() : null)
                .build();
    }

    // 저금통 상세 조회
    @Override
    public PiggyBankDetailResponseDto getDetail(Long piggyBankId, Long walletId) {
        PiggyBank p = piggyBankMapper.selectById(piggyBankId);
        // 팀 공용 예외 처리
        if (p == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        // 소유권 검증: 이 저금통이 요청자의 지갑 소속인지
        if (!p.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        long target = p.getTargetAmount() == null ? 0 : p.getTargetAmount();
        long saved = p.getBalance() == null ? 0 : p.getBalance();
        int rate = target == 0 ? 0 : (int) (saved * 100 / target);
        long remaining = Math.max(0, target - saved);

        return PiggyBankDetailResponseDto.builder()
                .piggyBankId(p.getId())
                .name(p.getName())
                .icon(p.getIcon())
                .status(p.getStatus())
                .favorite(p.getIsFavorite())
                .savedAmount(saved)
                .targetAmount(target)
                .progressRate(rate)
                .remainingAmount(remaining)
                .bonusType(p.getBonusType() == null ? "NONE" : p.getBonusType().name())
                .bonusValue(p.getBonusValue())
                .bonusPaidAt(p.getBonusPaidAt())
                .build();
    }

    @Override
    @Transactional
    public PiggyBankDetailResponseDto create(Long childId, Long walletId, PiggyBankCreateRequestDto request) {
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
                .icon(request.getIcon())
                .targetAmount(request.getTargetAmount())
                .build();
        piggyBankMapper.insert(piggyBank);
        Long newId = piggyBankMapper.selectLastInsertId();

        // 3) 부모에게 저금통 생성 알림
        Long parentId = userMapper.selectParentIdByChildId(childId);
        if (parentId != null) {
            notificationService.create(
                    parentId,
                    NotificationType.PIGGY_BANK_CREATED,
                    String.format("자녀가 '%s' 저금통을 만들었어요.", request.getName())
            );
        }

        // 4) 생성된 id로 상세 반환
        return getDetail(newId, walletId);
    }

    // 저금통 삭제
    @Override
    @Transactional
    public void close(Long piggyBankId, Long childId, Long walletId) {
        PiggyBank p = piggyBankMapper.selectById(piggyBankId);
        if (p == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        // 소유권 검증
        if (!p.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        // 이미 최종 달성(ACHIEVE) or 취소(CANCEL)면 중도 포기 불가
        String status = p.getStatus();
        if ("ACHIEVE".equals(status) || "CANCEL".equals(status)) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_ALREADY_CLOSED);
        }
        // 1) 상태 CANCEL
        piggyBankMapper.cancel(piggyBankId);

        // 2) 환급: 저금통 원금을 지갑으로 (잔액 있을 때만)
        long refund = (p.getBalance() == null) ? 0 : p.getBalance();
        if (refund > 0) {
            String traceId = UUID.randomUUID().toString();     // 저금통 ↔ 지갑 거래 연결용
            piggyBankMapper.zeroBalance(piggyBankId);           // 저금통 잔액 0
            transferService.transferPiggyBankToWallet(childId, refund, traceId);  // 지갑 반영
            // 저금통 출금 이력 (같은 traceId로 지갑 거래와 연결, before=원금 after=0)
            piggyBankHistoryMapper.insertWithdrawal(piggyBankId, refund, refund, 0L, traceId);

            notificationService.create(
                    childId,
                    NotificationType.PIGGY_BANK_REFUNDED,
                    String.format("%s 저금통이 중도 해지되어 %,d원이 지갑으로 환급됐어요.", p.getName(), refund)
            );

            Long parentId = userMapper.selectParentIdByChildId(childId);
            if (parentId != null) {
                notificationService.create(
                        parentId,
                        NotificationType.PIGGY_BANK_REFUNDED,
                        String.format("자녀의 %s 저금통이 중도 해지되어 %,d원이 환급됐어요.", p.getName(), refund)
                );
            }
        }

    }

    @Override
    @Transactional
    public int promoteAchievements() {
        List<PiggyBankRefundTarget> targets = piggyBankMapper.selectPromotable();

        for (PiggyBankRefundTarget target : targets) {
            promote(target.getPiggyBankId(), target.getChildId(), target.getBalance());
        }

        return targets.size();
    }

    @Override
    @Transactional
    public boolean promoteIfDue(Long piggyBankId) {
        PiggyBank p = piggyBankMapper.selectById(piggyBankId);

        // 승격 대상(목표금액 도달 후 대기 중)이 아니면 스케줄러에 맡긴다
        if (p == null || !"PENDING_ACHIEVE".equals(p.getStatus()) || p.getFirstDepositedAt() == null) {
            return false;
        }

        // selectPromotable과 같은 기준(최초입금일 + 7일)으로 판단
        boolean periodElapsed = !LocalDate.now().isBefore(p.getFirstDepositedAt().toLocalDate().plusDays(7));
        if (!periodElapsed) {
            return false;
        }

        Long childId = piggyBankMapper.selectChildIdByPiggyBankId(piggyBankId);
        promote(piggyBankId, childId, p.getBalance());

        return true;
    }

    // 승격(ACHIEVE) + 잔액 환급 공용 처리
    private void promote(Long piggyBankId, Long childId, Long balance) {
        piggyBankMapper.promoteById(piggyBankId);

        long refund = balance == null ? 0 : balance;
        if (refund > 0) {
            String traceId = UUID.randomUUID().toString();
            piggyBankMapper.zeroBalance(piggyBankId);
            transferService.transferPiggyBankToWallet(childId, refund, traceId);
            piggyBankHistoryMapper.insertWithdrawal(piggyBankId, refund, refund, 0L, traceId);
        }
    }

    // 저금통 즐겨찾기 토글 (아이당 1개만)
    @Override
    @Transactional
    public boolean toggleFavorite(Long piggyBankId, Long walletId) {
        PiggyBank p = piggyBankMapper.selectById(piggyBankId);
        if (p == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }
        // 소유권 검증
        if (!p.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        boolean next = !Boolean.TRUE.equals(p.getIsFavorite());
        if (next) {
            // 1개 제한: 같은 지갑 기존 즐겨찾기 해제 후 이 저금통만 등록
            piggyBankMapper.clearFavoritesByWallet(walletId);
            piggyBankMapper.updateFavorite(piggyBankId, true);
        } else {
            // 다시 누르면 해제 (0개 허용)
            piggyBankMapper.updateFavorite(piggyBankId, false);
        }
        return next;
    }
}
