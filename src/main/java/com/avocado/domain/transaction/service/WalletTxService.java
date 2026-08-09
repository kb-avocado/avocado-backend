package com.avocado.domain.transaction.service;

import com.avocado.global.response.PageResponse;
import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxListItemResponseDto;

public interface WalletTxService {

    PageResponse<WalletTxListItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto request
    );

}
