package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.piggybank.service.PiggyBankBonusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/piggybanks")
@RequiredArgsConstructor
@Api(tags = "저금통 보너스 API")
public class PiggyBankBonusController {

    private final PiggyBankBonusService piggyBankBonusService;

    @PutMapping("/{piggyBankId}/bonus")
    @ApiOperation(
            value = "저금통 보너스 설정",
            notes = "퍼센트(RATE) 또는 정액(FIXED) 중 하나로 보너스를 설정합니다. 1회만 설정 가능합니다."
    )
    public ApiResponse<PiggyBankBonusResponseDto> setBonus(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankBonusSetRequestDto request
    ) {
        PiggyBankBonusResponseDto response = piggyBankBonusService.setBonus(piggyBankId, request);

        return ApiResponse.success(
                "PIGGY_BANK_BONUS_SET",
                "보너스가 설정되었습니다.",
                response
        );
    }

    @PostMapping("/{piggyBankId}/bonus/pay")
    @ApiOperation(
            value = "저금통 보너스 지급 처리",
            notes = "목표를 달성한 저금통의 보너스 지급을 완료 처리합니다. 실제 송금은 별도 송금 기능에서 이루어지고, 이 API는 지급 완료 상태만 기록합니다."
    )
    public ApiResponse<PiggyBankBonusPayResponseDto> payBonus(
            @PathVariable Long piggyBankId
    ) {
        PiggyBankBonusPayResponseDto response = piggyBankBonusService.payBonus(piggyBankId);

        return ApiResponse.success("PIGGY_BANK_BONUS_PAID", "보너스 지급 처리가 완료되었습니다.", response);
    }
}