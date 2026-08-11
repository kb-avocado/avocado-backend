package com.avocado.domain.wallet.service;

import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {

    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;

    /*
     * childId 기준으로 자녀 선불지갑 단건 정보를 조회한다.
     * 인증 사용자 확인, 자녀 존재 확인, 조회 권한 검증, 지갑 존재 확인을 순서대로 처리한다.
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

        if (!walletMapper.existsChildById(childId)) {
            throw new BusinessException(CHILD_NOT_FOUND);
        }

        validateChildWalletAccess(childId, authUser);

        WalletVo wallet = walletMapper.findByChildId(childId)
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        return WalletResponseDto.from(wallet);
    }

    /**
     * 부모 외부 계좌에서 들어온 금액을
     * 아이 선불지갑에 입금한다.
     */
    @Override
    @Transactional
    public void depositFromAccount(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        // 1. 아이 소유의 지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(
                walletId,
                childId
        );

        // 2. 거래 전/후 잔액을 계산한다.
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore + amount;

        // 3. 실제 지갑 잔액을 증가시킨다.
        increaseBalance(
                walletId,
                amount
        );

        // 4. 선불지갑 거래 이력을 생성한다.
        Long historyId = createAccountChargeHistory(
                walletId,
                traceId,
                amount
        );

        // 5. 실제 잔액 변화를 원장에 기록한다.
        createIncomingLedger(
                historyId,
                walletId,
                amount,
                balanceBefore,
                balanceAfter
        );

    }

    @Override
    @Transactional
    public void withdrawForPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        // 아이 선불 지갑을 잠금 조회
        WalletVo wallet = getActiveWalletForUpdate(
                walletId,
                childId
        );

        // 저금할 금액보다 지갑 잔액이 적은지 확인
        if (wallet.getBalance() < amount) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        // 거래 전/후 잔액을 계산한다.
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - amount;

        // 실제 지갑 잔액을 차감
        decreaseBalance(
                walletId,
                amount
        );

        // 5. 지갑 거래 이력을 생성
        Long historyId = createPiggyBankDepositHistory(
                walletId,
                traceId,
                amount
        );

        // 6. 지갑에서 돈이 빠져나갔으므로 OUT 원장을 생성
        createOutgoingLedger(
                historyId,
                walletId,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    private void createIncomingLedger(
            Long historyId,
            Long walletId,
            Long amount,
            long balanceBefore,
            long balanceAfter
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

    private Long createAccountChargeHistory(
            Long walletId,
            String traceId,
            Long amount
    ) {
        WalletHistoryVo walletHistory = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType("CHARGE")
                .amount(amount)
                .memo("부모 계좌 충전")
                .status("SUCCESS")
                .build();

        int insertedRows = walletTxMapper.insertWalletHistory(walletHistory);

        if (insertedRows != 1 || walletHistory.getId() == null) {
            throw new BusinessException(WALLET_HISTORY_CREATE_FAILED);
        }

        return walletHistory.getId();
    }

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

    /*
     * 로그인 사용자가 조회 대상 자녀 본인이거나,
     * 해당 자녀와 ACTIVE 가족 관계로 연결된 보호자인지 검증한다.
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

    /*
     * 로그인 사용자가 CHILD 유형이고,
     * 본인의 childId로 지갑을 조회하는 경우인지 확인한다.
     */
    private boolean isChildOwner(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.CHILD.equals(authUser.getUserType())
                && childId.equals(authUser.getUserId());
    }

    /*
     * 로그인 사용자가 PARENT 유형이고,
     * 조회 대상 자녀와 ACTIVE 가족 관계가 있는지 확인한다.
     */
    private boolean isConnectedParent(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.PARENT.equals(authUser.getUserType())
                && walletMapper.existsActiveFamilyRelation(
                authUser.getUserId(),
                childId
        );
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
     * 저금통 저축으로 발생한
     * 선불지갑 거래 이력을 생성한다.
     */
    private Long createPiggyBankDepositHistory(
            Long walletId,
            String traceId,
            Long amount
    ) {
        WalletHistoryVo walletHistory = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType("PIGGY_BANK_DEPOSIT")
                .amount(amount)
                .memo("저금통 저축")
                .status("SUCCESS")
                .build();

        int insertedRows = walletTxMapper.insertWalletHistory(walletHistory);

        if (insertedRows != 1 || walletHistory.getId() == null) {
            throw new BusinessException(WALLET_HISTORY_CREATE_FAILED);
        }

        return walletHistory.getId();
    }

    /**
     * 지갑에서 빠져나간 금액을
     * OUT 원장으로 기록한다.
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
}
