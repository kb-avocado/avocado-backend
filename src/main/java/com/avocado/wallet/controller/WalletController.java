package com.avocado.wallet.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.wallet.dto.response.WalletResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@Api(tags = "아이 선불 지갑 API")
public class WalletController {

    @GetMapping("/{childId}")
    @ApiOperation(
            value = "아이 선불 지갑 조회",
            notes = "특정 아이의 선불지갑 정보(상태, 잔액 등)를 조회합니다."
    )
    public ApiResponse<WalletResponse> getChildWallet(@PathVariable Long childId) {
        WalletResponse mockWalletResponse = WalletResponse.builder()
                .id(1L)
                .childId(childId)
                .balance(35000)
                .status("ACTIVE")
                .createdAt("2026-07-29 10:49:06.123456")
                .updatedAt("2026-07-29 10:49:06.123456")
                .build();

        return ApiResponse.success(
                "CHILD_WALLET_FOUND",
                "아이의 선불 지갑을 조회했습니다.",
                mockWalletResponse
        );
    }


}
