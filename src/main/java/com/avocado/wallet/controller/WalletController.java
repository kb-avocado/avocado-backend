package com.avocado.wallet.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.wallet.dto.response.WalletResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/wallets")
@Api(tags = "아이 선불 지갑 API")
public class WalletController {

    @GetMapping("/{childId}")
    @ApiOperation(
            value = "아이 선불 지갑 조회",
            notes = "특정 아이의 선불지갑 정보(상태, 잔액 등)를 조회합니다."
    )
    public ApiResponse<WalletResponseDto> getChildWallet(@PathVariable Long childId) {
        WalletResponseDto mockWalletResponse = WalletResponseDto.builder()
                .walletId(1L)
                .childId(childId)
                .balance(35000L)
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return ApiResponse.success(
                "CHILD_WALLET_FOUND",
                "아이의 선불 지갑을 조회했습니다.",
                mockWalletResponse
        );
    }


}
