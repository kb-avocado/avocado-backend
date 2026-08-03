package com.avocado.wallet.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.wallet.dto.response.WalletQrResponse;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@Api(tags = "아이 선불 지갑 QR 코드 API")
public class WalletQrController {

    @PostMapping("/qr")
    public ApiResponse<WalletQrResponse> createWalletQr(@PathVariable Long walletId) {
        WalletQrResponse mockData = WalletQrResponse.builder()
                .id(1L)
                .walletId(walletId)
                .token("QR_8f24dbfa7c934f20a3bc91e7")
                .status("ACTIVE")
                .expiredAt("2026-07-28T12:35:00")
                .createdAt("2026-07-28T12:32:00")
                .build();

        return ApiResponse.success(
                "WALLET_QR_CREATED",
                "결제용 QR코드를 발급했습니다.",
                mockData
        );
    }
}
