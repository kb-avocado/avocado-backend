package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.service.AccountService;
import com.avocado.domain.family.service.FamilyService;
import com.avocado.domain.transaction.service.AccountTxService;
import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.user.service.UserService;
import com.avocado.domain.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceImplTest {

    @Mock
    private AccountService accountService;

    @Mock
    private AccountTxService accountTxService;

    @Mock
    private FamilyService familyService;

    @Mock
    private UserService userService;

    @Mock
    private WalletService walletService;

    @InjectMocks
    private TransferServiceImpl transferService;

    private Long parentId;
    private Long childId;
    private Long accountId;
    private Long amount;
    private String traceId;

    @BeforeEach
    void setUp() {
        // ACTIVE 부모 회원 김민준 ID
        parentId = 101L;

        // 김민준과 ACTIVE 가족 관계인 자녀 김지원 ID
        childId = 102L;

        // 김민준이 보유한 ACTIVE 계좌 ID
        accountId = 1001L;

        // 테스트 송금 금액
        amount = 10_000L;

        // 내부 서비스 호출에 사용할 거래 추적 ID
        traceId = "test-account-to-wallet-trace-id";
    }

    /**
     * 내부 호출용 부모 계좌-자녀 선불지갑 송금이 정상 수행되는지 검증한다.
     */
    @Test
    @DisplayName("내부 호출 - 부모 계좌에서 자녀 선불지갑으로 정상 송금한다")
    void transferAccountToWallet_internal_success() {
        // 부모의 ACTIVE 계좌 조회 결과를 준비한다.
        AccountVo account = mock(AccountVo.class);

        when(account.getId())
                .thenReturn(accountId);

        when(accountService.getActiveAccount(parentId))
                .thenReturn(account);

        // 부모 계좌에서 자녀 선불지갑으로 송금한다.
        transferService.transferAccountToWallet(
                parentId,
                childId,
                amount,
                traceId
        );

        // ACTIVE 부모 회원 검증이 호출됐는지 확인한다.
        verify(userService)
                .validateActiveParent(parentId);

        // ACTIVE 자녀 회원 검증이 호출됐는지 확인한다.
        verify(userService)
                .validateActiveChild(childId);

        // 부모의 ACTIVE 계좌를 조회했는지 확인한다.
        verify(accountService)
                .getActiveAccount(parentId);

        // 부모와 자녀의 ACTIVE 가족 관계를 검증했는지 확인한다.
        verify(familyService)
                .validateActiveRelation(
                        parentId,
                        childId
                );

        // 부모 계좌의 송금 거래 이력을 기록했는지 확인한다.
        verify(accountTxService)
                .recordWalletCharge(
                        accountId,
                        traceId,
                        amount
                );

        // 자녀의 선불지갑에 송금 금액을 입금했는지 확인한다.
        verify(walletService)
                .depositFromAccount(
                        childId,
                        amount,
                        traceId
                );
    }

    /**
     * 요청 DTO 기반 부모 계좌-자녀 선불지갑 송금 결과가 정상 반환되는지 검증한다.
     */
    @Test
    @DisplayName("API 호출 - 부모 계좌에서 자녀 선불지갑으로 정상 송금한다")
    void transferAccountToWallet_request_success() {
        // 송금 요청 DTO를 준비한다.
        AccountToWalletTransferRequestDto requestDto =
                mock(AccountToWalletTransferRequestDto.class);

        when(requestDto.getParentId())
                .thenReturn(parentId);

        when(requestDto.getChildId())
                .thenReturn(childId);

        when(requestDto.getAmount())
                .thenReturn(amount);

        // 부모의 ACTIVE 계좌 조회 결과를 준비한다.
        AccountVo account = mock(AccountVo.class);

        when(account.getId())
                .thenReturn(accountId);

        when(accountService.getActiveAccount(parentId))
                .thenReturn(account);

        // 송금 결과에 사용할 자녀 이름을 준비한다.
        when(userService.getUserName(childId))
                .thenReturn("김지원");

        // 송금을 실행한다.
        TransferResultVo result =
                transferService.transferAccountToWallet(requestDto);

        // 송금 상대방 이름을 검증한다.
        assertThat(result.getCounterpartyName())
                .isEqualTo("김지원");

        // 송금 금액을 검증한다.
        assertThat(result.getAmount())
                .isEqualTo(amount);

        // 부모의 ACTIVE 계좌를 조회했는지 확인한다.
        verify(accountService)
                .getActiveAccount(parentId);

        // 부모와 자녀의 ACTIVE 가족 관계를 검증했는지 확인한다.
        verify(familyService)
                .validateActiveRelation(
                        parentId,
                        childId
                );

        // 송금 결과에 사용할 자녀 이름을 조회했는지 확인한다.
        verify(userService)
                .getUserName(childId);

        // 부모 계좌 거래 이력을 기록했는지 확인한다.
        verify(accountTxService)
                .recordWalletCharge(
                        eq(accountId),
                        anyString(),
                        eq(amount)
                );

        // 자녀 선불지갑에 금액을 입금했는지 확인한다.
        verify(walletService)
                .depositFromAccount(
                        eq(childId),
                        eq(amount),
                        anyString()
                );
    }

    /**
     * 자녀 선불지갑에서 저금통으로 송금 요청을 정상 전달하는지 검증한다.
     */
    @Test
    @DisplayName("자녀 선불지갑에서 저금통으로 정상 송금한다")
    void transferWalletToPiggyBank_success() {
        // 자녀 선불지갑에서 저금통으로 송금한다.
        transferService.transferWalletToPiggyBank(
                childId,
                amount,
                traceId
        );

        // WalletService의 저금통 출금 처리가 호출됐는지 확인한다.
        verify(walletService)
                .withdrawForPiggyBank(
                        childId,
                        amount,
                        traceId
                );
    }

    /**
     * 저금통에서 자녀 선불지갑으로 송금 요청을 정상 전달하는지 검증한다.
     */
    @Test
    @DisplayName("저금통에서 자녀 선불지갑으로 정상 송금한다")
    void transferPiggyBankToWallet_success() {
        // 저금통에서 자녀 선불지갑으로 송금한다.
        transferService.transferPiggyBankToWallet(
                childId,
                amount,
                traceId
        );

        // WalletService의 저금통 반환금 입금 처리가 호출됐는지 확인한다.
        verify(walletService)
                .depositFromPiggyBank(
                        childId,
                        amount,
                        traceId
                );
    }
}