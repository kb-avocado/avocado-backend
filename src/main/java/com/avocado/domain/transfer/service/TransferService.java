package com.avocado.domain.transfer.service;

import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResultResponseDto;
import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.transfer.dto.request.WalletToPiggyBankTransferRequestDto;

public interface TransferService {

    // 부모 계좌에서 아이 선불지갑으로 송금한다.
    TransferResultVo transferAccountToWallet(
            AccountToWalletTransferRequestDto requestDto
    );

    // 아이 선불지갑에서 아이 저금통으로 송금한다.
    PiggyBankDepositResultResponseDto transferWalletToPiggyBank(
            WalletToPiggyBankTransferRequestDto requestDto
    );

    // 아이 저금통에서 아이 선불지갑으로 송금한다.

}
