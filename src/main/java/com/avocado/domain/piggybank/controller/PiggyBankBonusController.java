package com.avocado.domain.piggybank.controller;

import com.avocado.domain.wallet.service.WalletService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.dto.request.PiggyBankBonusSetRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusPayResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankBonusResponseDto;
import com.avocado.domain.piggybank.service.PiggyBankBonusService;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.PIGGY_BANK_BONUS_PAID;
import static com.avocado.global.response.code.SuccessCode.PIGGY_BANK_BONUS_SET;

@RestController
@RequestMapping("/api/piggybanks")
@RequiredArgsConstructor
@Api(tags = "저금통 보너스 API")
public class PiggyBankBonusController {

    private final PiggyBankBonusService piggyBankBonusService;
    private final WalletService walletService;

    @PutMapping("/{piggyBankId}/bonus")
    @ApiOperation(
            value = "저금통 보너스 설정",
            notes = "퍼센트(RATE) 또는 정액(FIXED) 중 하나로 보너스를 설정합니다. 1회만 설정 가능합니다."
    )
    public ResponseEntity<ApiResponse<PiggyBankBonusResponseDto>> setBonus(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankBonusSetRequestDto request,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long targetChildId = (childId != null) ? childId : authUser.getUserId();
        Long walletId = walletService.getChildWallet(targetChildId, authUser).getWalletId();

        PiggyBankBonusResponseDto response = piggyBankBonusService.setBonus(piggyBankId, request, walletId, targetChildId);

        return ResponseEntity
                .status(PIGGY_BANK_BONUS_SET.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_BONUS_SET, response));
    }

    @PostMapping("/{piggyBankId}/bonus/pay")
    @ApiOperation(
            value = "저금통 보너스 지급 처리",
            notes = "목표를 달성한 저금통의 보너스 지급을 완료 처리합니다. 실제 송금은 별도 송금 기능에서 이루어지고, 이 API는 지급 완료 상태만 기록합니다."
    )
    public ResponseEntity<ApiResponse<PiggyBankBonusPayResponseDto>> payBonus(
            @PathVariable Long piggyBankId,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long targetChildId = (childId != null) ? childId : authUser.getUserId();
        Long walletId = walletService.getChildWallet(targetChildId, authUser).getWalletId();

        PiggyBankBonusPayResponseDto response = piggyBankBonusService.payBonus(piggyBankId, walletId);

        return ResponseEntity
                .status(PIGGY_BANK_BONUS_PAID.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_BONUS_PAID, response));
    }
}