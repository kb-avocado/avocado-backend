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
     * 부모 외부 계좌에서 연결된 자녀의 선불지갑으로 금액을 송금한다.
     *
     * @param requestDto 계좌-선불지갑 송금 요청 정보
     * @return 송금 상대방과 송금 금액 정보
     */
    @Override
    @Transactional
    public TransferResultVo transferAccountToWallet(
            AccountToWalletTransferRequestDto requestDto
    ) {
        Long parentId = requestDto.getParentId();
        Long childId = requestDto.getChildId();
        Long amount = requestDto.getAmount();

        // 부모 계정 활성화 검증
        userService.validateActiveParent(parentId);

        // 아이 계정 활성화 검증
        userService.validateActiveChild(childId);

        // 부모에게 연결된 ACTIVE 외부 계좌를 조회한다.
        AccountVo account = accountService.getActiveAccount(parentId);

        // 계좌 아이디
        Long accountId = account.getId();

        // 송금 결과에 표시할 아이 이름을 조회한다.
        String childName = userService.getUserName(childId);

        // 계좌 거래와 지갑 거래를 연결하기 위한 공통 추적 ID를 생성한다.
        String traceId = UUID.randomUUID().toString();

        // 부모와 아이가 ACTIVE 가족 관계인지 검증한다.
        familyService.validateActiveRelation(
                parentId,
                childId
        );

        // 부모 외부 계좌 사용 이력을 기록한다.
        accountTxService.recordWalletCharge(
                accountId,
                traceId,
                amount
        );

        // 아이 선불지갑에 금액을 입금하고 지갑 거래 이력 및 원장을 함께 생성한다.
        walletService.depositFromAccount(
                childId,
                amount,
                traceId
        );

        // 송금 결과를 반환한다.
        return TransferResultVo.builder()
                .counterpartyName(childName)
                .amount(amount)
                .build();
    }

    /**
     * 부모 외부 계좌에서 연결된 자녀의 선불지갑으로 금액을 송금한다.
     *
     * @param parentId 송금하는 부모 회원 ID
     * @param childId 송금받는 자녀 회원 ID
     * @param amount 송금 금액
     * @param traceId 연관 거래 추적 ID
     */
    @Override
    @Transactional
    public void transferAccountToWallet(
            Long parentId,
            Long childId,
            Long amount,
            String traceId
    ) {
        // 부모 계정 활성화 검증
        userService.validateActiveParent(parentId);

        // 아이 계정 활성화 검증
        userService.validateActiveChild(childId);

        // 부모 활성화 계좌 조회
        AccountVo account = accountService.getActiveAccount(parentId);

        // 계좌 아이디 
        Long accountId = account.getId();

        // 부모 아이 관계 체크
        familyService.validateActiveRelation(
                parentId,
                childId
        );

        // 계좌에서 금액 출금은 우리 서비스 영역 밖의 기능으로, 기록만 남긴다.
        accountTxService.recordWalletCharge(
                accountId,
                traceId,
                amount
        );

        // 지갑으로 금액 입금
        walletService.depositFromAccount(
                childId,
                amount,
                traceId
        );
    }

    /**
     * 자녀 선불지갑에서 저금통으로 금액을 송금한다.
     *
     * @param childId 송금하는 자녀 회원 ID
     * @param amount 송금 금액
     * @param traceId 연관 거래 추적 ID
     */
    @Override
    @Transactional
    public void transferWalletToPiggyBank(
            Long childId,
            Long amount,
            String traceId
    ) {
        walletService.withdrawForPiggyBank(
                childId,
                amount,
                traceId
        );
    }

    /**
     * 저금통에서 출금된 금액을 자녀 선불지갑에 입금한다.
     *
     * @param childId 입금받는 자녀 회원 ID
     * @param amount 입금 금액
     * @param traceId 연관 거래 추적 ID
     */
    @Override
    @Transactional
    public void transferPiggyBankToWallet(
            Long childId,
            Long amount,
            String traceId
    ) {
        walletService.depositFromPiggyBank(
                childId,
                amount,
                traceId
        );
    }
}