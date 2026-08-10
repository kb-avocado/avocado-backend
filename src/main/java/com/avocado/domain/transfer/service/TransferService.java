package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.wallet.domain.WalletVo;

public interface TransferService {

    // 부모 계좌에서 아이 선불지갑으로 송금한다.
    TransferResultVo transferAccountToWallet(
            AccountToWalletTransferRequestDto requestDto
    );


}
