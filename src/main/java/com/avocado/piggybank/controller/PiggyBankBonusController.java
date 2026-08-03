package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.piggybank.service.PiggyBankBonusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}