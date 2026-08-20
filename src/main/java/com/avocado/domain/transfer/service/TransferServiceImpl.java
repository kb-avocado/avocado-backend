package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.domain.BankCode;
import com.avocado.domain.account.service.AccountService;
import com.avocado.domain.family.service.FamilyService;
import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.transaction.service.AccountTxService;
import com.avocado.domain.transaction.service.WalletTxService;
import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.transfer.dto.request.TransferRecipientListRequestDto;
import com.avocado.domain.transfer.dto.request.WalletTransferRequestDto;
import com.avocado.domain.transfer.dto.response.AccountToWalletTransferResponseDto;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.dto.response.WalletTransferResponseDto;
import com.avocado.domain.user.service.UserService;
import com.avocado.domain.wallet.domain.WalletBalanceChangeVo;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.service.WalletService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.avocado.global.response.code.ErrorCode.INVALID_REQUEST;
import static com.avocado.global.response.code.ErrorCode.WALLET_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferServiceImpl implements TransferService {

    private final AccountService accountService;
    private final AccountTxService accountTxService;
    private final FamilyService familyService;
    private final UserService userService;
    private final WalletService walletService;
    private final WalletTxService walletTxService;
    private final NotificationService notificationService;

    /**
     * 부모 외부 계좌에서 연결된 자녀의 선불지갑으로 금액을 송금한다.
     *
     * @param requestDto 계좌-선불지갑 송금 요청 정보
     * @return 송금 상대방과 송금 금액 정보
     */
    @Override
    @Transactional
    public AccountToWalletTransferResponseDto transferAccountToWallet(
            Long parentId,
            AccountToWalletTransferRequestDto requestDto
    ) {
        Long childId = requestDto.getChildId();
        Long amount = requestDto.getAmount();

        // 부모 계정 활성화 검증
        userService.validateActiveParent(parentId);

        // 아이 계정 활성화 검증
        userService.validateActiveChild(childId);

        // 부모에게 연결된 ACTIVE 외부 계좌를 조회한다.
        AccountVo account = accountService.getActiveAccount(parentId);

        // 계좌 ID
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

        notificationService.create(
                childId,
                NotificationType.ALLOWANCE_RECEIVED,
                String.format("%,d원이 입금되었습니다.", amount)
        );


        TransferResultVo transferResultVo = TransferResultVo.builder()
                .counterpartyName(childName)
                .amount(amount)
                .build();

        return AccountToWalletTransferResponseDto.from(transferResultVo);
    }

    /**
     * 부모 외부 계좌에서 연결된 자녀의 선불지갑으로 금액을 송금한다.
     *
     * @param parentId 송금하는 부모 회원 ID
     * @param childId  송금받는 자녀 회원 ID
     * @param amount   송금 금액
     * @param traceId  연관 거래 추적 ID
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

        // 계좌 ID
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

        notificationService.create(
                childId,
                NotificationType.ALLOWANCE_RECEIVED,
                String.format("%,d원이 입금되었습니다.", amount)
        );
    }

    /**
     * 자녀 선불지갑에서 저금통으로 금액을 송금한다.
     *
     * @param childId 송금하는 자녀 회원 ID
     * @param amount  송금 금액
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
     * @param amount  입금 금액
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

    /**
     * 아이 선불지갑에서 내부 선불지갑 또는 외부 계좌로 송금한다.
     *
     * @param childId    송금하는 아이 회원 ID
     * @param requestDto 송금 요청 정보
     * @return 송금 처리 결과
     */
    @Override
    @Transactional
    public WalletTransferResponseDto transferFromWallet(
            Long childId,
            WalletTransferRequestDto requestDto
    ) {
        // 송금하는 회원이 ACTIVE 아이인지 검증
        userService.validateActiveChild(childId);

        BankCode bankCode;

        try {
            bankCode = BankCode.fromCode(requestDto.getBankCode());
        } catch (IllegalArgumentException e) {
            throw new BusinessException(INVALID_REQUEST);
        }

        // 송금에 사용되는 공통 추적 ID를 생성
        String traceId = UUID.randomUUID().toString();

        // 아보카도 코드이면 내부 지갑 송금으로 처리
        if (bankCode == BankCode.AVOCADO) {
            return transferWalletToWallet(
                    childId,
                    requestDto,
                    traceId
            );
        }

        // 아보카도 이외 은행이면 외부 계좌 송금으로 처리
        return transferWalletToAccount(
                childId,
                requestDto,
                traceId
        );
    }

    /**
     * 아이 선불지갑에서 다른 아보카도 선불지갑으로 송금한다.
     *
     * @param childId    송금하는 아이 회원 ID
     * @param requestDto 송금 요청 정보
     * @param traceId    연관 거래 추적 ID
     * @return 송금 처리 결과
     */
    private WalletTransferResponseDto transferWalletToWallet(
            Long childId,
            WalletTransferRequestDto requestDto,
            String traceId
    ) {
        // 송금자의 ACTIVE 선불지갑을 조회
        WalletVo senderSnapshot = walletService.getActiveWallet(childId);

        // 입력받은 지갑번호로 수취자의 ACTIVE 선불지갑을 조회
        WalletVo receiverSnapshot = walletService.getActiveWalletByNumber(
                requestDto.getRecipientNumber()
        );

        // 자신의 지갑으로 송금 불가
        if (senderSnapshot.getId().equals(receiverSnapshot.getId())) {
            throw new BusinessException(INVALID_REQUEST);
        }

        // 수취 지갑의 소유자가 ACTIVE 아이인지 검증
        userService.validateActiveChild(receiverSnapshot.getChildId());

        // 두 거래가 반대 방향으로 동시에 발생해도 잠금 순서가 같도록 ID를 정렬
        Long firstWalletId = Math.min(
                senderSnapshot.getId(),
                receiverSnapshot.getId()
        );

        Long secondWalletId = Math.max(
                senderSnapshot.getId(),
                receiverSnapshot.getId()
        );

        // 두 선불지갑을 ID 순서대로 배타적 잠금
        List<WalletVo> lockedWallets = walletService.getWalletsForUpdate(
                firstWalletId,
                secondWalletId
        );

        // 잠금 조회 결과에서 실제 송금자 지갑
        WalletVo senderWallet = lockedWallets.stream()
                .filter(wallet -> wallet.getId().equals(senderSnapshot.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        // 잠금 조회 결과에서 실제 수취자 지갑
        WalletVo receiverWallet = lockedWallets.stream()
                .filter(wallet -> wallet.getId().equals(receiverSnapshot.getId()))
                .findFirst()
                .orElseThrow(() -> new BusinessException(WALLET_NOT_FOUND));

        Long amount = requestDto.getAmount();

        // 송금자 지갑의 잔액을 감소
        WalletBalanceChangeVo senderBalance = walletService.withdraw(
                senderWallet,
                amount
        );

        // 수취자 지갑의 잔액을 증가
        WalletBalanceChangeVo receiverBalance = walletService.deposit(
                receiverWallet,
                amount
        );

        // 거래 당시 표시할 송금자 실제 회원 이름을 조회
        String senderName = userService.getUserName(childId);
        // 거래 당시 표시할 실제 수취자 회원 이름을 조회
        String receiverName = userService.getUserName(receiverWallet.getChildId());

        // 송금자의 TRANSFER_OUT 거래와 OUT 원장을 기록한다.
        walletTxService.recordTransferOutToWallet(
                senderWallet.getId(),
                traceId,
                amount,
                receiverWallet.getId(),
                receiverName,
                senderBalance.getBalanceBefore(),
                senderBalance.getBalanceAfter()
        );

        // 수취자의 TRANSFER_IN 거래와 IN 원장을 기록한다.
        walletTxService.recordTransferInFromWallet(
                receiverWallet.getId(),
                traceId,
                amount,
                senderWallet.getId(),
                senderName,
                receiverBalance.getBalanceBefore(),
                receiverBalance.getBalanceAfter()
        );

        return WalletTransferResponseDto.builder()
                .recipientName(receiverName)
                .bankCode(BankCode.AVOCADO.getCode())
                .recipientNumber(receiverWallet.getWalletNumber())
                .amount(amount)
                .balance(senderBalance.getBalanceAfter())
                .transferredAt(LocalDateTime.now())
                .build();
    }

    /**
     * 아이 선불지갑에서 외부 은행 계좌로 송금한다.
     *
     * @param childId    송금하는 아이 회원 ID
     * @param requestDto 송금 요청 정보
     * @param traceId    연관 거래 추적 ID
     * @return 송금 처리 결과
     */
    private WalletTransferResponseDto transferWalletToAccount(
            Long childId,
            WalletTransferRequestDto requestDto,
            String traceId
    ) {
        String bankCode = requestDto.getBankCode();
        String accountNumber = requestDto.getRecipientNumber();
        Long amount = requestDto.getAmount();

        // 수취 계좌가 서비스에 등록된 ACTIVE 부모 계좌인지 조회
        Optional<AccountVo> registeredAccount = accountService.findActiveAccount(
                bankCode,
                accountNumber
        );

        // 서비스에 등록된 계좌이면 부모의 실제 이름을 사용
        String recipientName = registeredAccount
                .map(account -> userService.getUserName(account.getUserId()))
                .orElse(requestDto.getRecipientName());

        // 송금자의 지갑을 배타적 잠금으로 조회
        WalletVo senderWallet = walletService.getActiveWalletForUpdate(
                childId
        );

        // 송금자의 실제 선불지갑 잔액을 감소
        WalletBalanceChangeVo senderBalance = walletService.withdraw(
                senderWallet,
                amount
        );

        // 아이 선불지갑의 TRANSFER_OUT 거래와 OUT 원장을 기록
        walletTxService.recordTransferOutToAccount(
                senderWallet.getId(),
                traceId,
                amount,
                bankCode,
                accountNumber,
                recipientName,
                senderBalance.getBalanceBefore(),
                senderBalance.getBalanceAfter()
        );

        // 수취 계좌가 서비스에 등록된 부모 계좌이면 DEPOSIT 이력도 기록
        registeredAccount.ifPresent(account ->
                accountTxService.recordWalletDeposit(
                        account.getId(),
                        traceId,
                        amount
                )
        );

        return WalletTransferResponseDto.builder()
                .recipientName(recipientName)
                .bankCode(bankCode)
                .recipientNumber(accountNumber)
                .amount(amount)
                .balance(senderBalance.getBalanceAfter())
                .transferredAt(LocalDateTime.now())
                .build();
    }
}
