package com.avocado.domain.wallet.controller;

import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.SuccessCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.service.WalletService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
@Api(tags = "아이 선불 지갑 API")
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/{childId}")
    @ApiOperation(
            value = "아이 선불 지갑 조회",
            notes = "특정 아이의 선불지갑 정보(상태, 잔액 등)를 조회합니다."
    )
    public ResponseEntity<ApiResponse<WalletResponseDto>> getChildWallet(
            @PathVariable Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        WalletResponseDto response = walletService.getChildWallet(childId, authUser);
        return ResponseEntity
                .status(SuccessCode.CHILD_WALLET_FOUND.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.CHILD_WALLET_FOUND, response));
    }
}
