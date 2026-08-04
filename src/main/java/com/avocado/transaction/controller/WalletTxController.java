package com.avocado.transaction.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.common.response.PageResponse;
import com.avocado.transaction.dto.response.WalletTxListItemResponseDto;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "거래 내역 API")
@RequestMapping("/api/wallets")
@RestController
@RequiredArgsConstructor
public class WalletTxController {

    //
    @RequestMapping("/transactions")
    public ApiResponse<PageResponse<WalletTxListItemResponseDto>> getTransactions() {

    }
}
