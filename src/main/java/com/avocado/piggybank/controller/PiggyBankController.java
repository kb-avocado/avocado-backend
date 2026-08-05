package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.common.response.code.SuccessCode;
import com.avocado.piggybank.dto.request.PiggyBankCreateRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.piggybank.service.PiggyBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.avocado.common.response.code.SuccessCode.*;

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
    public ResponseEntity<ApiResponse<PiggyBankListResponseDto>> getList(
            @RequestParam(defaultValue = "IN_PROGRESS") String status,
            // TODO: 로그인(인증) 붙으면 로그인 사용자의 walletId로 대체
            @RequestParam Long walletId
    ) {
        PiggyBankListResponseDto response =
                piggyBankService.getList(walletId, status);

        return ResponseEntity
                .status(PIGGY_BANK_LIST_FETCHED.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_LIST_FETCHED, response));
    }

    @GetMapping("/{piggyBankId}")
    @ApiOperation(value = "저금통 상세 조회", notes = "저금통 하나의 상세 정보를 조회합니다.")
    public ResponseEntity<ApiResponse<PiggyBankDetailResponseDto>> getDetail(
            @PathVariable Long piggyBankId
    ) {
        PiggyBankDetailResponseDto response =
                piggyBankService.getDetail(piggyBankId);

        return ResponseEntity
                .status(PIGGY_BANK_DETAIL_FETCHED.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_DETAIL_FETCHED, response));
    }

    //저금통 생성
    @PostMapping
    @ApiOperation(value = "저금통 생성", notes = "새 저금통을 생성합니다. (최대 3개)")
    public ResponseEntity<ApiResponse<PiggyBankDetailResponseDto>> create(
            @RequestParam Long walletId,   // TODO: 인증 붙으면 로그인 사용자 walletId로 교체
            @Valid @RequestBody PiggyBankCreateRequestDto request
    ) {
        PiggyBankDetailResponseDto response =
                piggyBankService.create(walletId, request);

        return ResponseEntity
                .status(PIGGY_BANK_CREATED.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_CREATED, response));
    }

    //저금통 삭제
    @PostMapping("/{piggyBankId}/close")
    @ApiOperation(value = "저금통 중도 포기", notes = "목표 달성 전 저금통을 중도 포기합니다. (환급은 후속 예정)")
    public ResponseEntity<ApiResponse<Void>> close(
            @PathVariable Long piggyBankId
    ) {
        piggyBankService.close(piggyBankId);

        return ResponseEntity
                .status(PIGGY_BANK_CLOSED.getHttpStatus())
                .body(ApiResponse.success(PIGGY_BANK_CLOSED));
    }
}