package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.common.response.code.SuccessCode;
import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.piggybank.service.PiggyBankDepositService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/piggybanks/{piggyBankId}/deposits")
@RequiredArgsConstructor
@Api(tags = "저금통 거래내역 API")
public class PiggyBankDepositController {

    private final PiggyBankDepositService piggyBankDepositService;

    @GetMapping
    @ApiOperation(value = "저금통 거래내역 조회", notes = "저금통의 입금 내역을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PiggyBankDepositResponseDto>>> getDeposits(
            @PathVariable Long piggyBankId
    ) {
        List<PiggyBankDepositResponseDto> deposits = piggyBankDepositService.getDeposits(piggyBankId);

        return ResponseEntity
                .status(SuccessCode.DEPOSIT_HISTORY_FETCHED.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.DEPOSIT_HISTORY_FETCHED, deposits));
    }
}