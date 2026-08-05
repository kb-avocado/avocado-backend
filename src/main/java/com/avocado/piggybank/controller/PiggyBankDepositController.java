package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.piggybank.dto.request.PiggyBankDepositRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankDepositResultResponseDto;
import com.avocado.piggybank.service.PiggyBankDepositService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/piggybanks/{piggyBankId}/deposits")
@RequiredArgsConstructor
@Api(tags = "저금통 거래내역 API")
public class PiggyBankDepositController {

    private final PiggyBankDepositService piggyBankDepositService;

    @GetMapping
    @ApiOperation(value = "저금통 거래내역 조회", notes = "저금통의 입금 내역을 조회합니다.")
    public ApiResponse<List<PiggyBankDepositResponseDto>> getDeposits(
            @PathVariable Long piggyBankId
    ) {
        List<PiggyBankDepositResponseDto> deposits = piggyBankDepositService.getDeposits(piggyBankId);

        return ApiResponse.success("DEPOSIT_HISTORY_FETCHED", "거래 내역 조회에 성공했습니다.", deposits);
    }

    @PostMapping
    @ApiOperation(value = "저금통 입금", notes = "저금통에 자유롭게 입금합니다. 지갑 잔액 차감 연동은 추후 반영됩니다.")
    public ApiResponse<PiggyBankDepositResultResponseDto> deposit(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankDepositRequestDto request
    ) {
        PiggyBankDepositResultResponseDto response = piggyBankDepositService.deposit(piggyBankId, request);

        return ApiResponse.success("PIGGY_BANK_DEPOSITED", "입금이 완료되었습니다.", response);
    }
}