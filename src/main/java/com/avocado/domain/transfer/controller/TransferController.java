package com.avocado.domain.transfer.controller;

import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.transfer.dto.response.AccountToWalletTransferResponseDto;
import com.avocado.domain.transfer.service.TransferService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.SuccessCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/account-to-wallet")
    public ResponseEntity<ApiResponse<AccountToWalletTransferResponseDto>> transferAccountToWallet(
            @AuthenticationPrincipal
            AuthUser authUser,
            @Valid
            @RequestBody
            AccountToWalletTransferRequestDto requestDto
    ) {
        AccountToWalletTransferResponseDto response = transferService.transferAccountToWallet(
                authUser.getUserId(),
                requestDto
        );

        return ResponseEntity
                .status(SuccessCode.TRANSFER_SUCCESS.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.TRANSFER_SUCCESS, response));
    }
}
