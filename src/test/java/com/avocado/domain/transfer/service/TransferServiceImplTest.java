package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.BankCode;
import com.avocado.domain.account.service.AccountService;
import com.avocado.domain.family.service.FamilyService;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.transaction.service.AccountTxService;
import com.avocado.domain.transaction.service.WalletTxService;
import com.avocado.domain.transfer.dto.request.WalletTransferRequestDto;
import com.avocado.domain.transfer.dto.response.WalletTransferResponseDto;
import com.avocado.domain.user.service.UserService;
import com.avocado.domain.wallet.domain.WalletBalanceChangeVo;
import com.avocado.domain.wallet.domain.WalletStatus;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Mock
    private WalletTxService walletTxService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransferServiceImpl transferService;

    private WalletVo senderWallet;
    private WalletVo receiverWallet;

    @BeforeEach
    void setUp() {
        senderWallet = WalletVo.builder()
                .id(1L)
                .childId(100L)
                .walletNumber("111111111111")
                .balance(50_000L)
                .status(WalletStatus.ACTIVE)
                .build();

        receiverWallet = WalletVo.builder()
                .id(2L)
                .childId(200L)
                .walletNumber("222222222222")
                .balance(10_000L)
                .status(WalletStatus.ACTIVE)
                .build();
    }

    /**
     * 내부 선불지갑 송금 시 양쪽 지갑 잔액과 거래 기록이 모두 처리되는지 검증한다.
     */
    @Test
    @DisplayName("아이 선불지갑에서 다른 아이 선불지갑으로 송금한다")
    void transferWalletToWallet() {
        // given
        WalletTransferRequestDto request = WalletTransferRequestDto.builder()
                .bankCode(BankCode.AVOCADO.getCode())
                .recipientNumber("222222222222")
                .recipientName("수취인")
                .amount(10_000L)
                .build();

        when(walletService.getActiveWallet(100L))
                .thenReturn(senderWallet);

        when(walletService.getActiveWalletByNumber("222222222222"))
                .thenReturn(receiverWallet);

        when(walletService.getWalletsForUpdate(1L, 2L))
                .thenReturn(List.of(senderWallet, receiverWallet));

        when(walletService.withdraw(senderWallet, 10_000L))
                .thenReturn(
                        WalletBalanceChangeVo.builder()
                                .walletId(1L)
                                .balanceBefore(50_000L)
                                .balanceAfter(40_000L)
                                .build()
                );

        when(walletService.deposit(receiverWallet, 10_000L))
                .thenReturn(
                        WalletBalanceChangeVo.builder()
                                .walletId(2L)
                                .balanceBefore(10_000L)
                                .balanceAfter(20_000L)
                                .build()
                );

        when(userService.getUserName(100L))
                .thenReturn("김아이");

        when(userService.getUserName(200L))
                .thenReturn("박아이");

        // when
        WalletTransferResponseDto response =
                transferService.transferFromWallet(
                        100L,
                        request
                );

        // then
        assertThat(response.getRecipientName()).isEqualTo("박아이");
        assertThat(response.getBankCode()).isEqualTo("999");
        assertThat(response.getRecipientNumber()).isEqualTo("222222222222");
        assertThat(response.getAmount()).isEqualTo(10_000L);
        assertThat(response.getBalance()).isEqualTo(40_000L);

        // 송금자 잔액 감소 확인
        verify(walletService).withdraw(
                senderWallet,
                10_000L
        );

        // 수취자 잔액 증가 확인
        verify(walletService).deposit(
                receiverWallet,
                10_000L
        );

        // 송금자의 거래 기록 확인
        verify(walletTxService).recordTransferOutToWallet(
                eq(1L),
                anyString(),
                eq(10_000L),
                eq(2L),
                eq("박아이"),
                eq(50_000L),
                eq(40_000L)
        );

        // 수취자의 거래 기록 확인
        verify(walletTxService).recordTransferInFromWallet(
                eq(2L),
                anyString(),
                eq(10_000L),
                eq(1L),
                eq("김아이"),
                eq(10_000L),
                eq(20_000L)
        );

        // 내부 지갑 송금에서는 계좌 거래가 발생하지 않는다.
        verifyNoInteractions(accountTxService);
    }
}