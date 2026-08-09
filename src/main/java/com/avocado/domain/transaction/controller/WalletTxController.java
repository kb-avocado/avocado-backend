package com.avocado.domain.transaction.controller;

import com.avocado.domain.transaction.service.WalletTxService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "거래 내역 API")
@RequestMapping("/api/wallets")
@RestController
@RequiredArgsConstructor
public class WalletTxController {

    private final WalletTxService service;

//    @GetMapping("/transactions")
//    public ApiResponse<PageResponse<WalletTxListItemResponseDto>> getTransactions() {
//    }
}
