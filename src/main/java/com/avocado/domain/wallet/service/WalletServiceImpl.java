package com.avocado.domain.wallet.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {
    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;
    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;

    /**
     * childId 기준으로 자녀 선불지갑 단건 정보를 조회한다.
     */
    @Override
    public WalletResponseDto getChildWallet(
            Long childId,
            AuthUser authUser
    ) {
        // TODO: /api/wallets/** permitAll 제거 후에도 방어 로직으로 유지한다.
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        // 조회 대상 회원이 실제 아이 회원인지 확인한다.
        if (!userMapper.existsChildById(childId)) {
            throw new BusinessException(CHILD_NOT_FOUND);
        }

        // 아이 본인 또는 ACTIVE 가족 관계의 보호자인지 검증한다.
        validateChildWalletAccess(
                childId,
                authUser
        );

        // 아이의 선불지갑을 조회한다.
        WalletVo wallet = walletMapper
                .findByChildId(childId)
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        return WalletResponseDto.from(wallet);
    }

    /**
     * 부모 계좌에서 들어온 금액을 아이 선불지갑에 입금한다.
     */
    @Override
    @Transactional
    public void depositFromAccount(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        deposit(
                childId,
                walletId,
                amount,
                traceId,
                "CHARGE",
                "부모 계좌 충전"
        );
    }

    /**
     * 아이 선불지갑에서 저금통으로 저금할 금액을 출금한다.
     * 지갑 잔액을 차감하고,
     * PIGGY_BANK_DEPOSIT 거래 이력과 OUT 원장을 기록한다.
     */
    @Override
    @Transactional
    public void withdrawForPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        // 아이 소유의 선불지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(
                walletId,
                childId
        );

        // 출금하려는 금액보다 지갑 잔액이 부족한지 확인한다.
        if (wallet.getBalance() < amount) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        // 거래 전/후 잔액을 계산한다.
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - amount;

        // 실제 선불지갑 잔액을 차감한다.
        decreaseBalance(
                walletId,
                amount
        );

        // 저금통 저축으로 발생한 지갑 거래 이력을 생성한다.
        Long historyId = createWalletHistory(
                walletId,
                traceId,
                "PIGGY_BANK_DEPOSIT",
                amount,
                "저금통 저축"
        );

        // 지갑에서 금액이 빠져나갔으므로 OUT 원장을 생성한다.
        createOutgoingLedger(
                historyId,
                walletId,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 만기 또는 중도 해지된 저금통에서 반환된 금액을 아이 선불지갑에 입금한다.
     */
    @Override
    @Transactional
    public void depositFromPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        deposit(
                childId,
                walletId,
                amount,
                traceId,
                "PIGGY_BANK_WITHDRAWAL",
                "저금통 해지 반환"
        );
    }

    /**
     * 선불지갑으로 금액이 들어오는 공통 입금 처리를 수행한다.
     */
    private void deposit(
            Long childId,
            Long walletId,
            Long amount,
            String traceId,
            String transactionType,
            String memo
    ) {
        // 아이 소유의 ACTIVE 선불지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(
                walletId,
                childId
        );

        // 거래 전/후 잔액을 계산한다.
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore + amount;

        // 실제 선불지갑 잔액을 증가시킨다.
        increaseBalance(
                walletId,
                amount
        );

        // 선불지갑 거래 이력을 생성한다.
        Long historyId = createWalletHistory(
                walletId,
                traceId,
                transactionType,
                amount,
                memo
        );

        // 지갑으로 금액이 들어왔으므로 IN 원장을 생성한다.
        createIncomingLedger(
                historyId,
                walletId,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 아이 소유의 선불지갑을 잠금 조회하고 사용 가능한 ACTIVE 상태인지 검증한다.
     */
    private WalletVo getActiveWalletForUpdate(
            Long walletId,
            Long childId
    ) {
        WalletVo wallet = walletMapper
                .findForUpdateByIdAndChildId(
                        walletId,
                        childId
                )
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new BusinessException(WALLET_INACTIVE);
        }

        return wallet;
    }

    /**
     * 선불지갑 잔액을 증가시킨다.
     */
    private void increaseBalance(
            Long walletId,
            Long amount
    ) {
        int updatedRows = walletMapper.increaseBalance(
                walletId,
                amount
        );

        if (updatedRows != 1) {
            throw new BusinessException(WALLET_UPDATE_FAILED);
        }
    }

    /**
     * 선불지갑 잔액을 차감한다.
     */
    private void decreaseBalance(
            Long walletId,
            Long amount
    ) {
        int updatedRows = walletMapper.decreaseBalance(
                walletId,
                amount
        );

        if (updatedRows != 1) {
            throw new BusinessException(WALLET_UPDATE_FAILED);
        }
    }

    /**
     * 선불지갑 거래 이력을 생성한다.
     *
     * @return 생성된 wallet_histories PK
     */
    private Long createWalletHistory(
            Long walletId,
            String traceId,
            String transactionType,
            Long amount,
            String memo
    ) {
        WalletHistoryVo walletHistory = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(transactionType)
                .amount(amount)
                .memo(memo)
                .status("SUCCESS")
                .build();

        int insertedRows = walletTxMapper.insertWalletHistory(
                walletHistory
        );

        // INSERT에 실패했거나 generated key를 받지 못한 경우
        // 이후 ledger를 생성할 수 없으므로 예외를 발생시킨다.
        if (insertedRows != 1 || walletHistory.getId() == null) {
            throw new BusinessException(WALLET_HISTORY_CREATE_FAILED);
        }

        return walletHistory.getId();
    }

    /**
     * 선불지갑으로 들어온 금액을 IN 원장으로 기록한다.
     */
    private void createIncomingLedger(
            Long historyId,
            Long walletId,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                "IN",
                amount,
                balanceBefore,
                balanceAfter
        );

        if (insertedRows != 1) {
            throw new BusinessException(WALLET_LEDGER_CREATE_FAILED);
        }
    }

    /**
     * 선불지갑에서 빠져나간 금액을 OUT 원장으로 기록한다.
     */
    private void createOutgoingLedger(
            Long historyId,
            Long walletId,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                "OUT",
                amount,
                balanceBefore,
                balanceAfter
        );

        if (insertedRows != 1) {
            throw new BusinessException(WALLET_LEDGER_CREATE_FAILED);
        }
    }

    /**
     * 로그인 사용자가 조회 대상 아이 본인이거나,
     * 해당 아이와 ACTIVE 가족 관계로 연결된 보호자인지 검증한다.
     */
    private void validateChildWalletAccess(
            Long childId,
            AuthUser authUser
    ) {
        if (isChildOwner(childId, authUser)) {
            return;
        }

        if (isConnectedParent(childId, authUser)) {
            return;
        }

        throw new BusinessException(FORBIDDEN);
    }

    /**
     * 로그인 사용자가 CHILD 유형이고, 조회 대상 아이 본인인지 확인한다.
     */
    private boolean isChildOwner(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.CHILD.equals(
                authUser.getUserType()
        ) && childId.equals(
                authUser.getUserId()
        );
    }

    /**
     * 로그인 사용자가 PARENT 유형이고, 조회 대상 아이와 ACTIVE 가족 관계인지 확인한다.
     */
    private boolean isConnectedParent(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.PARENT.equals(
                authUser.getUserType()
        ) && familyRelationMapper.existsActiveRelation(
                authUser.getUserId(),
                childId
        );
    }
}