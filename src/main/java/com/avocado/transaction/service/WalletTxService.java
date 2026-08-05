package com.avocado.transaction.service;

import com.avocado.common.response.PageResponse;
import com.avocado.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.transaction.dto.response.WalletTxListItemResponseDto;

public interface WalletTxService {

    PageResponse<WalletTxListItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto request
    );

}
