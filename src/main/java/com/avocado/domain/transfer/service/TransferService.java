package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;

public interface TransferService {

    // 부모 계좌에서 아이 선불지갑으로 송금한다.
    TransferResultVo transferAccountToWallet(
            AccountToWalletTransferRequestDto requestDto
    );

    // 저금통에 보낼 금액만 지갑에서 출금한다.
    void transferWalletToPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    );

    // 저금통에서 출금된 금액을 지갑에 반영한다.
    void transferPiggyBankToWallet(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    );
}
