package com.avocado.domain.transfer.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@NoArgsConstructor
public class WalletToPiggyBankTransferRequestDto {
    // 저금을 수행하는 아이 회원 ID
    @NotNull
    private Long childId;

    // 돈을 출금할 아이 선불지갑 ID
    @NotNull
    private Long walletId;

    // 돈을 입금할 저금통 ID
    @NotNull
    private Long piggyBankId;

    // 저금할 금액
    @NotNull
    @Positive
    private Long amount;
}
