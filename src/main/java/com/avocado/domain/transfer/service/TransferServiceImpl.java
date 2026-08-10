package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.mapper.AccountMapper;
import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferServiceImpl implements TransferService {

    private final FamilyRelationMapper familyRelationMapper;
    private final AccountMapper accountMapper;
    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public TransferResultVo transferAccountToWallet(AccountToWalletTransferRequestDto requestDto) {
        Long parentId = requestDto.getParentId();
        Long childId = requestDto.getChildId();
        Long walletId = requestDto.getWalletId();
        Long amount = requestDto.getAmount();

        // 1. 부모 연동 계좌 조회
        AccountVo account = getActiveParentAccount(parentId);

        // 2. 부모와 아이의 가족 관계 확인
        validateFamilyRelation(
                parentId,
                childId
        );

        // 3. 송금 상대방 이름 조회
        String childName = getChildName(childId);

        // 4. 아이 선불지갑 조회 및 잠금
        WalletVo wallet = getActiveWalletForUpdate(
                walletId,
                childId
        );

        // 5. 송금 전/후 지갑 잔액 계산
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore + amount;

        // 6. 계좌와 지갑 거래를 연결하기 위한 추적 ID 생성
        String traceId = UUID.randomUUID().toString();

        // 7. 부모 계좌 거래 이력 생성
        createAccountHistory(
                account.getId(),
                traceId,
                amount
        );

        // 8. 아이 선불지갑 잔액 증가
        increaseWalletBalance(
                walletId,
                amount
        );

        // 9. 선불지갑 거래 이력 생성
        Long walletHistoryId = createWalletHistory(
                walletId,
                traceId,
                amount
        );

        // 10. 선불지갑 원장 생성
        createWalletLedger(
                walletHistoryId,
                walletId,
                amount,
                balanceBefore,
                balanceAfter
        );

        // 11. 송금 결과 반환
        return TransferResultVo.builder()
                .counterpartyName(childName)
                .amount(amount)
                .build();
    }

    // 부모에게 연결된 ACTIVE 외부 계좌를 조회
    private AccountVo getActiveParentAccount(Long parentId) {
        return accountMapper
                .findActiveByUserId(parentId)
                .orElseThrow(() -> new BusinessException(ACTIVE_PARENT_NOT_FOUND));
    }

    // 부모와 아이가 실제 가족 관계인지 검증
    private void validateFamilyRelation(
            Long parentId,
            Long childId
    ) {
        boolean isFamily = familyRelationMapper.existsActiveRelation(
                parentId,
                childId
        );

        if (!isFamily) {
            throw new BusinessException(FAMILY_RELATION_NOT_FOUND);
        }
    }

    // 송금 상대방인 아이의 이름을 조회
    private String getChildName(Long childId) {
        return userMapper
                .findNameById(childId)
                .orElseThrow(() -> new BusinessException(CHILD_NOT_FOUND));
    }

    // 아이 소유의 선불지갑 잠금 조회 및 사용 가능 상태인지 검증
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

    // 부모의 외부 연동 계좌를 사용한 거래 이력을 기록
    private void createAccountHistory(
            Long accountId,
            String traceId,
            Long amount
    ) {
        int insertedRows = accountMapper.insertWalletChargeHistory(
                accountId,
                traceId,
                amount
        );

        if (insertedRows != 1) {
            throw new BusinessException(ACCOUNT_HISTORY_CREATE_FAILED);
        }
    }

    // 아이 선불지갑 잔액을 증가
    private void increaseWalletBalance(
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

    // 선불지갑 거래 이력 생성 및 생성된 거래 이력 ID를 반환
    private Long createWalletHistory(
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

    // 선불지갑 잔액 변화를 원장에 기록
    private void createWalletLedger(
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
}