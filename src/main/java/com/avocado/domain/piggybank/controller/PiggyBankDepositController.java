package com.avocado.domain.piggybank.controller;

import com.avocado.domain.piggybank.dto.request.PiggyBankDepositRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResultResponseDto;
import com.avocado.domain.piggybank.service.PiggyBankDepositService;
import com.avocado.domain.transfer.dto.request.WalletToPiggyBankTransferRequestDto;
import com.avocado.domain.transfer.service.TransferService;
import com.avocado.domain.wallet.service.WalletService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

import static com.avocado.global.response.code.SuccessCode.*;

@RestController
@RequestMapping("/api/piggybanks/{piggyBankId}/deposits")
@RequiredArgsConstructor
@Api(tags = "저금통 거래내역 API")
public class PiggyBankDepositController {

    private final PiggyBankDepositService piggyBankDepositService;
    private final WalletService walletService;
    private final TransferService transferService;

    @GetMapping
    @ApiOperation(value = "저금통 거래내역 조회", notes = "저금통의 입금 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PiggyBankDepositResponseDto>>> getDeposits(
            @PathVariable Long piggyBankId
    ) {
        List<PiggyBankDepositResponseDto> deposits = piggyBankDepositService.getDeposits(piggyBankId);

        return ResponseEntity
                .status(DEPOSIT_HISTORY_FETCHED.getHttpStatus())
                .body(ApiResponse.success(DEPOSIT_HISTORY_FETCHED, deposits));
    }

    @PostMapping
    @ApiOperation(value = "저금통 입금", notes = "지갑에서 차감해 저금통에 입금합니다.")
    public ResponseEntity<ApiResponse<PiggyBankDepositResultResponseDto>> deposit(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankDepositRequestDto request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long childId = authUser.getUserId();
        Long walletId = walletService.getChildWallet(childId, authUser).getWalletId();

        WalletToPiggyBankTransferRequestDto transferRequest = WalletToPiggyBankTransferRequestDto.builder()
                .childId(childId)
                .walletId(walletId)
                .piggyBankId(piggyBankId)
                .amount(request.getAmount())
                .build();

//        PiggyBankDepositResultResponseDto response = transferService.transferWalletToPiggyBank(transferRequest);
//        transferService.transferWalletToPiggyBank();
//
//
//        return ResponseEntity
//                .status(PIGGY_BANK_DEPOSITED.getHttpStatus())
//                .body(ApiResponse.success(PIGGY_BANK_DEPOSITED, response));

        return null;
    }
}