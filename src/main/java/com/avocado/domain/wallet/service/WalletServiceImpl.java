package com.avocado.domain.wallet.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.merchant.domain.MerchantVo;
import com.avocado.domain.merchant.service.MerchantService;
import com.avocado.domain.payment.domain.PaymentRequestedResult;
import com.avocado.domain.payment.domain.PaymentSimulationResult;
import com.avocado.domain.transaction.domain.LedgerType;
import com.avocado.domain.transaction.domain.TransactionStatus;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.domain.WalletTransactionType;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletStatus;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.UUID;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletServiceImpl implements WalletService {
    // 선불지갑 번호 길이
    private static final int WALLET_NUMBER_LENGTH = 12;
    // 선불지갑 고유 번호 생성 시도 횟수
    private static final int MAX_WALLET_NUMBER_GENERATION_ATTEMPTS = 10;
    // 선불지갑 번호 생성에 사용하는 난수 생성기
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;
    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;
    private final MerchantService merchantService;

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
     * @param childId 입금 대상 자녀 회원 ID
     * @param amount  입금 금액
     * @param traceId 연관 거래 추적 ID
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
                WalletTransactionType.CHARGE,
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
     * 정상 상태가 된 아이 회원에게 고유 계좌번호를 가진 아보카도 선불지갑을 발급한다.
     *
     * @param childId 선불지갑을 발급할 아이 회원 ID
     */
    @Override
    @Transactional
    public void issueWallet(
            Long childId
    ) {
        // 이미 선불지갑을 보유하고 있는 아이일 경우
        if (walletMapper.existsByChildId(childId)) {
            throw new BusinessException(WALLET_ALREADY_EXISTS);
        }

        // 고유 선불지갑 번호를 생성
        String walletNumber = generateUniqueWalletNumber();

        // 초기 상태의 선불지갑을 구성
        WalletVo wallet = WalletVo.builder()
                .childId(childId)
                .walletNumber(walletNumber)
                .balance(0L)
                .status(WalletStatus.ACTIVE)
                .build();

        // 아이에게 생성한 선불지갑을 부여
        if (walletMapper.insert(wallet) != 1) {
            throw new BusinessException(WALLET_CREATE_FAILED);
        }
    }

    /**
     * 저금통 저축을 위해 자녀 선불지갑에서 금액을 출금하고 거래 이력과 원장을 기록한다.
     *
     * @param childId  선불지갑 소유 자녀 ID
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
                WalletTransactionType.PIGGY_BANK_DEPOSIT,
                amount,
                null,
                "저금통 저축"
        );

        // 지갑에서 금액이 빠져나갔으므로 OUT 원장을 생성한다.
        createLedger(
                historyId,
                walletId,
                LedgerType.OUT,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 저금통에서 반환된 금액을 자녀 선불지갑에 입금한다.
     *
     * @param childId 입금 대상 자녀 회원 ID
     * @param amount  저금통에서 반환된 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
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
                WalletTransactionType.PIGGY_BANK_WITHDRAWAL,
                "저금통 해지 반환"
        );
    }

    @Override
    @Transactional
    public PaymentSimulationResult processPosPayment(
            Long childId,
            Long merchantId,
            Long amount,
            PaymentRequestedResult requestedResult
    ) {
        WalletVo wallet = walletMapper.findForUpdateByChildId(childId)
                .orElse(null);

        if (wallet == null) {
            return failedPaymentWithoutHistory(
                    merchantId,
                    null,
                    amount,
                    null,
                    WALLET_NOT_FOUND
            );
        }

        MerchantVo merchant = merchantService.findById(merchantId)
                .orElse(null);

        if (merchant == null) {
            return failedPaymentWithoutHistory(
                    merchantId,
                    null,
                    amount,
                    wallet.getBalance(),
                    MERCHANT_NOT_FOUND
            );
        }

        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            return createFailedPaymentResult(
                    wallet,
                    merchant,
                    amount,
                    WALLET_INACTIVE
            );
        }

        if (!merchant.isActive()) {
            return createFailedPaymentResult(
                    wallet,
                    merchant,
                    amount,
                    MERCHANT_INACTIVE
            );
        }

        if (PaymentRequestedResult.FORCE_FAIL.equals(requestedResult)) {
            return createFailedPaymentResult(
                    wallet,
                    merchant,
                    amount,
                    FORCED_FAILURE
            );
        }

        if (wallet.getBalance() < amount) {
            return createFailedPaymentResult(
                    wallet,
                    merchant,
                    amount,
                    INSUFFICIENT_BALANCE
            );
        }

        if (merchant.isRestrictedForChild()) {
            return createFailedPaymentResult(
                    wallet,
                    merchant,
                    amount,
                    MERCHANT_RESTRICTED
            );
        }

        return createSuccessfulPaymentResult(
                wallet,
                merchant,
                amount
        );
    }

    /**
     * 선불지갑 입금과 거래 이력 및 IN 원장 생성을 공통 처리한다.
     *
     * @param childId         선불지갑 소유 자녀 ID
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
            WalletTransactionType transactionType,
            String memo
    ) {

        // 아이 소유의 ACTIVE 선불지갑을 잠금 조회한다.
        WalletVo wallet = getActiveWalletForUpdate(childId);

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
                null,
                memo
        );

        // 지갑으로 금액이 들어왔으므로 IN 원장을 생성한다.
        createLedger(
                historyId,
                walletId,
                LedgerType.IN,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 자녀 소유의 선불지갑을 잠금 조회하고 ACTIVE 상태인지 검증한다.
     *
     * @param childId 선불지갑 소유 자녀 ID
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
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
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
            WalletTransactionType transactionType,
            Long amount,
            Long merchantId,
            String memo
    ) {
        return createWalletHistory(
                walletId,
                traceId,
                transactionType,
                amount,
                merchantId,
                memo,
                TransactionStatus.SUCCESS,
                null
        );
    }

    private Long createWalletHistory(
            Long walletId,
            String traceId,
            WalletTransactionType transactionType,
            Long amount,
            Long merchantId,
            String memo,
            TransactionStatus status,
            ErrorCode failureCode
    ) {
        // 저장할 선불지갑 거래 이력을 생성한다.
        WalletHistoryVo walletHistory = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(transactionType)
                .amount(amount)
                .merchantId(merchantId)
                .memo(memo)
                .status(status)
                .failureCode(failureCode == null ? null : failureCode.name())
                .build();

        // 선불지갑 거래 이력을 저장한다.
        int insertedRows = walletTxMapper.insertWalletHistory(walletHistory);

        // 저장 실패 또는 generated key 누락 시 예외를 발생시킨다.
        if (insertedRows != 1 || walletHistory.getId() == null) {
            throw new BusinessException(WALLET_HISTORY_CREATE_FAILED);
        }

        return walletHistory.getId();
    }

    private PaymentSimulationResult createSuccessfulPaymentResult(
            WalletVo wallet,
            MerchantVo merchant,
            Long amount
    ) {
        long balanceBefore = wallet.getBalance();
        long balanceAfter = balanceBefore - amount;

        decreaseBalance(
                wallet.getId(),
                amount
        );

        Long historyId = createWalletHistory(
                wallet.getId(),
                newPaymentTraceId(),
                WalletTransactionType.PAYMENT,
                amount,
                merchant.getId(),
                "POS 결제",
                TransactionStatus.SUCCESS,
                null
        );

        createLedger(
                historyId,
                wallet.getId(),
                LedgerType.OUT,
                amount,
                balanceBefore,
                balanceAfter
        );

        return PaymentSimulationResult.builder()
                .walletHistoryId(historyId)
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .amount(amount)
                .status("SUCCESS")
                .balanceAfter(balanceAfter)
                .build();
    }

    private PaymentSimulationResult createFailedPaymentResult(
            WalletVo wallet,
            MerchantVo merchant,
            Long amount,
            ErrorCode failureCode
    ) {
        Long historyId = createWalletHistory(
                wallet.getId(),
                newPaymentTraceId(),
                WalletTransactionType.PAYMENT,
                amount,
                merchant.getId(),
                failureCode.getMessage(),
                TransactionStatus.FAILED,
                failureCode
        );

        return PaymentSimulationResult.builder()
                .walletHistoryId(historyId)
                .merchantId(merchant.getId())
                .merchantName(merchant.getName())
                .amount(amount)
                .status("FAILED")
                .failureCode(failureCode)
                .balanceAfter(wallet.getBalance())
                .build();
    }

    private PaymentSimulationResult failedPaymentWithoutHistory(
            Long merchantId,
            String merchantName,
            Long amount,
            Long balanceAfter,
            ErrorCode failureCode
    ) {
        return PaymentSimulationResult.builder()
                .merchantId(merchantId)
                .merchantName(merchantName)
                .amount(amount)
                .status("FAILED")
                .failureCode(failureCode)
                .balanceAfter(balanceAfter)
                .build();
    }

    private String newPaymentTraceId() {
        return "PAY-" + UUID.randomUUID();
    }

    /**
     * 선불지갑 거래의 잔액 증감 내역을 원장으로 기록한다.
     *
     * @param historyId     연결할 선불지갑 거래 이력 ID
     * @param walletId      선불지갑 ID
     * @param ledgerType    원장의 금액 증감 방향
     * @param amount        거래 금액
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter  거래 후 잔액
     * @throws BusinessException 원장 저장에 실패한 경우
     */
    private void createLedger(
            Long historyId,
            Long walletId,
            LedgerType ledgerType,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        // 선불지갑 거래의 잔액 증감 내용을 원장으로 저장한다.
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                ledgerType.name(),
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

    /**
     * 서비스에서 사용하지 않는 고유 지갑 계좌번호를 생성한다.
     *
     * @return 생성된 고유 지갑 계좌번호
     */
    private String generateUniqueWalletNumber() {
        // 선불지갑 고유 번호 충돌 시 최대 10회까지 새로운 번호를 생성
        for (int attempt = 0; attempt < MAX_WALLET_NUMBER_GENERATION_ATTEMPTS; attempt++) {
            String walletNumber = generateWalletNumber();

            // 사용 중이지 않은 번호이면 계좌번호로 확정
            if (!walletMapper.existsByWalletNumber(walletNumber)) {
                return walletNumber;
            }
        }

        // 선불지갑 고유 번호 생성 실패
        throw new BusinessException(WALLET_NUMBER_GENERATION_FAILED);
    }


    /**
     * 아보카도 선불지갑에서 사용할 12자리 숫자 계좌번호를 생성한다.
     *
     * @return 생성된 12자리 계좌번호
     */
    private String generateWalletNumber() {
        StringBuilder walletNumber = new StringBuilder(WALLET_NUMBER_LENGTH);

        // 첫 번째 자리는 1부터 9 사이의 숫자로 생성
        walletNumber.append(RANDOM.nextInt(9) + 1);

        // 나머지 11자리는 0부터 9 사이의 숫자로 생성
        for (int i = 1; i < WALLET_NUMBER_LENGTH; i++) {
            walletNumber.append(RANDOM.nextInt(10));
        }

        // 생성된 12자리의 번호를 반환
        return walletNumber.toString();
    }
}
