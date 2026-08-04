package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.piggybank.service.PiggyBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/piggybanks")
@RequiredArgsConstructor
@Api(tags = "저금통 API")
// 저금통 목록 조회
// GET /api/piggybanks?status=IN_PROGRESS&walletId=1
public class PiggyBankController {

    private final PiggyBankService piggyBankService;

    @GetMapping
    @ApiOperation(value = "저금통 목록 조회", notes = "상태별(IN_PROGRESS/CLOSED) 저금통 목록을 조회합니다.")
    public ApiResponse<PiggyBankListResponseDto> getList(
            @RequestParam(defaultValue = "IN_PROGRESS") String status,
            // TODO: 로그인(인증) 붙으면 로그인 사용자의 walletId로 대체
            @RequestParam Long walletId
    ) {
        PiggyBankListResponseDto response = piggyBankService.getList(walletId, status);

        return ApiResponse.success("PIGGYBANK_LIST_FETCHED", "저금통 목록 조회에 성공했습니다.", response);
    }
    @GetMapping("/{piggyBankId}")
    @ApiOperation(value = "저금통 상세 조회", notes = "저금통 하나의 상세 정보를 조회합니다.")
    public ApiResponse<PiggyBankDetailResponseDto> getDetail(@PathVariable Long piggyBankId) {
        PiggyBankDetailResponseDto response = piggyBankService.getDetail(piggyBankId);
        return ApiResponse.success("PIGGYBANK_DETAIL_FETCHED", "저금통 상세 조회에 성공했습니다.", response);
    }
}