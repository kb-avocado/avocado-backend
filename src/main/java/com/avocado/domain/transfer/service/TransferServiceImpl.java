package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.service.AccountService;
import com.avocado.domain.family.service.FamilyService;
import com.avocado.domain.transaction.service.AccountTxService;
import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.user.service.UserService;
import com.avocado.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final AccountTxService accountTxService;
    private final FamilyService familyService;
    private final UserService userService;
    private final WalletService walletService;

    /**
     * 부모 외부 계좌에서
     * 연결된 아이의 선불지갑으로 송금한다.
     * 계좌, 가족 관계, 회원, 지갑 도메인의 기능을 조합하고
     * 전체 송금의 트랜잭션 경계를 담당한다.
     */
    @Override
    @Transactional
    public TransferResultVo transferAccountToWallet(
            AccountToWalletTransferRequestDto requestDto
    ) {
        Long parentId = requestDto.getParentId();
        Long childId = requestDto.getChildId();
        Long walletId = requestDto.getWalletId();
        Long amount = requestDto.getAmount();

        // 1. 부모에게 연결된 ACTIVE 외부 계좌를 조회한다.
        AccountVo account = accountService.getActiveAccount(parentId);

        // 2. 부모와 아이가 ACTIVE 가족 관계인지 검증한다.
        familyService.validateActiveRelation(
                parentId,
                childId
        );

        // 3. 송금 결과에 표시할 아이 이름을 조회한다.
        String childName = userService.getUserName(childId);

        // 4. 계좌 거래와 지갑 거래를 연결하기 위한
        // 공통 추적 ID를 생성한다.
        String traceId = UUID.randomUUID().toString();

        // 5. 부모 외부 계좌 사용 이력을 기록한다.
        accountTxService.recordWalletCharge(
                account.getId(),
                traceId,
                amount
        );

        // 6. 아이 선불지갑에 금액을 입금하고
        // 지갑 거래 이력 및 원장을 함께 생성한다.
        walletService.depositFromAccount(
                childId,
                walletId,
                amount,
                traceId
        );

        // 7. 송금 결과를 반환한다.
        return TransferResultVo.builder()
                .counterpartyName(childName)
                .amount(amount)
                .build();
    }

    @Override
    @Transactional
    public void transferWalletToPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        walletService.withdrawForPiggyBank(
                childId,
                walletId,
                amount,
                traceId
        );
    }

    @Override
    @Transactional
    public void transferPiggyBankToWallet(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    ) {
        walletService.depositFromPiggyBank(
                childId,
                walletId,
                amount,
                traceId
        );
    }
}