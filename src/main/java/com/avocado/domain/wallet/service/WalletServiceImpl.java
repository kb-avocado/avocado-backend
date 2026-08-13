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
import com.avocado.global.response.code.ErrorCode;
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
     * 자녀 회원 ID를 기준으로 조회 권한을 검증하고 선불지갑 정보를 조회한다.
     *
     * @param childId  조회할 자녀 회원 ID
     * @param authUser 인증된 사용자 정보
     * @return 자녀의 선불지갑 정보
     * @throws BusinessException 인증 정보가 없거나 조회 권한 또는 지갑이 유효하지 않은 경우
     */
    @Override
    public WalletResponseDto getChildWallet(
            Long childId,
            AuthUser authUser
    ) {
        // 인증 정보가 없는 요청은 허용하지 않는다.
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        // 조회 대상 회원이 실제 자녀 회원인지 확인한다.
        if (!userMapper.existsChildById(childId)) {
            throw new BusinessException(CHILD_NOT_FOUND);
        }

        // 자녀 본인 또는 ACTIVE 가족 관계의 보호자인지 검증한다.
        validateChildWalletAccess(
                childId,
                authUser
        );

        // 자녀가 보유한 선불지갑을 조회한다.
        WalletVo wallet = walletMapper
                .findByChildId(childId)
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        return WalletResponseDto.from(wallet);
    }

    /**
     * 부모 계좌에서 전달된 금액을 자녀 선불지갑에 입금한다.
     *
     * @param childId  입금 대상 자녀 회원 ID
     * @param amount   입금 금액
     * @param traceId  연관 거래 추적 ID
     * @throws BusinessException 지갑 또는 거래 처리가 유효하지 않은 경우
     */
    @Override
    @Transactional
    public void depositFromAccount(
            Long childId,
            Long amount,
            String traceId
    ) {
        // 부모 계좌 충전 유형으로 공통 입금 처리를 수행한다.
        deposit(
                childId,
                amount,
                traceId,
                "CHARGE",
                "부모 계좌 충전"
        );
    }

    /**
     * 해당 선불지갑이 자녀 소유이며 ACTIVE 상태인지 검증한다.
     *
     * @param childId  선불지갑 소유 자녀 ID
     * @param walletId 확인할 선불지갑 ID
     * @throws BusinessException 해당 자녀의 ACTIVE 선불지갑이 아닌 경우
     */
    @Override
    public void validateActiveWallet(
            Long childId,
            Long walletId
    ) {
        // 해당 자녀가 소유한 ACTIVE 선불지갑인지 확인한다.
        boolean isActiveWallet = walletMapper.existsActiveByIdAndChildId(
                childId,
                walletId
        );

        // 유효한 선불지갑이 아니면 예외를 발생시킨다.
        if (!isActiveWallet) {
            throw new BusinessException(WALLET_INACTIVE);
        }
    }

    /**
     * 자녀 회원 ID로 ACTIVE 상태의 선불지갑을 조회한다.
     *
     * @param childId 조회할 자녀 회원 ID
     * @return ACTIVE 상태의 선불지갑 정보
     * @throws BusinessException ACTIVE 선불지갑이 존재하지 않는 경우
     */
    @Override
    public WalletVo getActiveWallet(
            Long childId
    ) {
        return walletMapper
                .findActiveByChildId(childId)
                .orElseThrow(() -> new BusinessException(WALLET_INACTIVE));
    }

    /**
     * 저금통 저축을 위해 자녀 선불지갑에서 금액을 출금하고 거래 이력과 원장을 기록한다.
     *
     * @param childId  선불지갑 소유 자녀 ID
     * @param walletId 출금 대상 선불지갑 ID
     * @param amount   출금 금액
     * @param traceId  저금통 거래와 연결하기 위한 추적 ID
     * @throws BusinessException 지갑이 유효하지 않거나 잔액이 부족하거나 거래 저장에 실패한 경우
     */
    @Override
    @Transactional
    public void withdrawForPiggyBank(
            Long childId,
            Long amount,
            String traceId
    ) {
        // 아이 소유의 ACTIVE 선불지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(
                childId
        );

        Long walletId = wallet.getId();

        // 출금 금액보다 지갑 잔액이 부족한지 확인한다.
        if (wallet.getBalance() < amount) {
            throw new BusinessException(INSUFFICIENT_BALANCE);
        }

        // 거래 전후 잔액을 계산한다.
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
     * 저금통에서 반환된 금액을 자녀 선불지갑에 입금한다.
     *
     * @param childId  입금 대상 자녀 회원 ID
     * @param amount   저금통에서 반환된 금액
     * @param traceId  저금통 거래와 연결하기 위한 추적 ID
     * @throws BusinessException 지갑 또는 거래 처리가 유효하지 않은 경우
     */
    @Override
    @Transactional
    public void depositFromPiggyBank(
            Long childId,
            Long amount,
            String traceId
    ) {
        // 저금통 출금 유형으로 공통 입금 처리를 수행한다.
        deposit(
                childId,
                amount,
                traceId,
                "PIGGY_BANK_WITHDRAWAL",
                "저금통 해지 반환"
        );
    }

    /**
     * 선불지갑 입금과 거래 이력 및 IN 원장 생성을 공통 처리한다.
     *
     * @param childId         선불지갑 소유 자녀 ID
     * @param walletId        입금 대상 선불지갑 ID
     * @param amount          입금 금액
     * @param traceId         연관 거래 추적 ID
     * @param transactionType 지갑 거래 유형
     * @param memo            거래 메모
     * @throws BusinessException 지갑 또는 거래 처리가 유효하지 않은 경우
     */
    private void deposit(
            Long childId,
            Long amount,
            String traceId,
            String transactionType,
            String memo
    ) {

        // 아이 소유의 ACTIVE 선불지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(
                childId
        );

        Long walletId = wallet.getId();

        // 거래 전후 잔액을 계산한다.
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
     * 자녀 소유의 선불지갑을 잠금 조회하고 ACTIVE 상태인지 검증한다.
     *
     * @param childId  선불지갑 소유 자녀 ID
     * @return 잠금 조회된 ACTIVE 선불지갑 정보
     * @throws BusinessException 지갑이 없거나 ACTIVE 상태가 아닌 경우
     */
    private WalletVo getActiveWalletForUpdate(
            Long childId
    ) {
        // 해당 자녀 소유의 선불지갑을 잠금 조회한다.
        WalletVo wallet = walletMapper
                .findForUpdateByChildId(
                        childId
                )
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        // ACTIVE 상태가 아닌 지갑은 사용할 수 없다.
        if (!"ACTIVE".equals(wallet.getStatus())) {
            throw new BusinessException(WALLET_INACTIVE);
        }

        return wallet;
    }

    /**
     * ACTIVE 선불지갑의 잔액을 지정한 금액만큼 증가시킨다.
     *
     * @param walletId 잔액을 증가시킬 선불지갑 ID
     * @param amount   증가시킬 금액
     * @throws BusinessException 잔액 변경에 실패한 경우
     */
    private void increaseBalance(
            Long walletId,
            Long amount
    ) {
        // 선불지갑 잔액을 증가시킨다.
        int updatedRows = walletMapper.increaseBalance(
                walletId,
                amount
        );

        // 정확히 한 행이 변경되지 않았다면 실패로 처리한다.
        if (updatedRows != 1) {
            throw new BusinessException(WALLET_UPDATE_FAILED);
        }
    }

    /**
     * ACTIVE 선불지갑의 잔액을 지정한 금액만큼 감소시킨다.
     *
     * @param walletId 잔액을 감소시킬 선불지갑 ID
     * @param amount   감소시킬 금액
     * @throws BusinessException 잔액 변경에 실패한 경우
     */
    private void decreaseBalance(
            Long walletId,
            Long amount
    ) {
        // 선불지갑 잔액을 감소시킨다.
        int updatedRows = walletMapper.decreaseBalance(
                walletId,
                amount
        );

        // 정확히 한 행이 변경되지 않았다면 실패로 처리한다.
        if (updatedRows != 1) {
            throw new BusinessException(WALLET_UPDATE_FAILED);
        }
    }

    /**
     * 선불지갑 거래 이력을 생성하고 생성된 거래 이력 ID를 반환한다.
     *
     * @param walletId        거래가 발생한 선불지갑 ID
     * @param traceId         연관 거래 추적 ID
     * @param transactionType 지갑 거래 유형
     * @param amount          거래 금액
     * @param memo            거래 메모
     * @return 생성된 wallet_histories PK
     * @throws BusinessException 거래 이력 저장 또는 생성 ID 조회에 실패한 경우
     */
    private Long createWalletHistory(
            Long walletId,
            String traceId,
            String transactionType,
            Long amount,
            String memo
    ) {
        // 저장할 선불지갑 거래 이력을 생성한다.
        WalletHistoryVo walletHistory = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(transactionType)
                .amount(amount)
                .memo(memo)
                .status("SUCCESS")
                .build();

        // 선불지갑 거래 이력을 저장한다.
        int insertedRows = walletTxMapper.insertWalletHistory(
                walletHistory
        );

        // 저장 실패 또는 generated key 누락 시 예외를 발생시킨다.
        if (insertedRows != 1 || walletHistory.getId() == null) {
            throw new BusinessException(WALLET_HISTORY_CREATE_FAILED);
        }

        return walletHistory.getId();
    }

    /**
     * 선불지갑으로 들어온 금액을 IN 원장으로 기록한다.
     *
     * @param historyId     연결할 선불지갑 거래 이력 ID
     * @param walletId      선불지갑 ID
     * @param amount        입금 금액
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter  거래 후 잔액
     * @throws BusinessException 원장 저장에 실패한 경우
     */
    private void createIncomingLedger(
            Long historyId,
            Long walletId,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        // 입금 거래를 IN 원장으로 저장한다.
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                "IN",
                amount,
                balanceBefore,
                balanceAfter
        );

        // 정확히 한 건이 저장되지 않았다면 실패로 처리한다.
        if (insertedRows != 1) {
            throw new BusinessException(WALLET_LEDGER_CREATE_FAILED);
        }
    }

    /**
     * 선불지갑에서 빠져나간 금액을 OUT 원장으로 기록한다.
     *
     * @param historyId     연결할 선불지갑 거래 이력 ID
     * @param walletId      선불지갑 ID
     * @param amount        출금 금액
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter  거래 후 잔액
     * @throws BusinessException 원장 저장에 실패한 경우
     */
    private void createOutgoingLedger(
            Long historyId,
            Long walletId,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        // 출금 거래를 OUT 원장으로 저장한다.
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                "OUT",
                amount,
                balanceBefore,
                balanceAfter
        );

        // 정확히 한 건이 저장되지 않았다면 실패로 처리한다.
        if (insertedRows != 1) {
            throw new BusinessException(WALLET_LEDGER_CREATE_FAILED);
        }
    }

    /**
     * 로그인 사용자가 자녀 본인이거나 ACTIVE 가족 관계의 보호자인지 검증한다.
     *
     * @param childId  조회 대상 자녀 회원 ID
     * @param authUser 인증된 사용자 정보
     * @throws BusinessException 조회 권한이 없는 경우
     */
    private void validateChildWalletAccess(
            Long childId,
            AuthUser authUser
    ) {
        // 로그인 사용자가 조회 대상 자녀 본인이면 허용한다.
        if (isChildOwner(childId, authUser)) {
            return;
        }

        // ACTIVE 가족 관계로 연결된 보호자이면 허용한다.
        if (isConnectedParent(childId, authUser)) {
            return;
        }

        // 조회 권한이 없으면 접근을 거부한다.
        throw new BusinessException(FORBIDDEN);
    }

    /**
     * 로그인 사용자가 CHILD 유형이며 조회 대상 자녀 본인인지 확인한다.
     *
     * @param childId  조회 대상 자녀 회원 ID
     * @param authUser 인증된 사용자 정보
     * @return 자녀 본인이면 true
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
     * 로그인 사용자가 PARENT 유형이며 자녀와 ACTIVE 가족 관계인지 확인한다.
     *
     * @param childId  조회 대상 자녀 회원 ID
     * @param authUser 인증된 사용자 정보
     * @return ACTIVE 가족 관계의 보호자이면 true
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