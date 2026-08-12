package com.avocado.domain.piggybank.controller;

import com.avocado.domain.wallet.service.WalletService;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.avocado.global.response.ApiResponse;
import com.avocado.domain.piggybank.dto.request.PiggyBankCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.domain.piggybank.service.PiggyBankService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.*;

@RestController
@RequestMapping("/api/piggybanks")
@RequiredArgsConstructor
@Api(tags = "저금통 API")
// 저금통 목록 조회
// GET /api/piggybanks?status=IN_PROGRESS&walletId=1
public class PiggyBankController {

    private final PiggyBankService piggyBankService;
    private final WalletService walletService;
    @GetMapping
    @ApiOperation(value = "저금통 목록 조회", notes = "상태별(IN_PROGRESS/CLOSED) 저금통 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<PiggyBankListResponseDto>> getList(
            @RequestParam(defaultValue = "IN_PROGRESS") String status,
            @RequestParam(required = false) Long childId,   // 부모가 아이 조회 시 사용 (아이는 생략)
            @AuthenticationPrincipal AuthUser authUser
    ) {
        // 아이는 본인, 부모는 childId로 아이 지정
        Long targetChildId = (childId != null) ? childId : authUser.getUserId();
        Long walletId = walletService.getChildWallet(targetChildId, authUser).getWalletId();

        PiggyBankListResponseDto response = piggyBankService.getList(walletId, status);

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
            @Valid @RequestBody PiggyBankCreateRequestDto request,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        Long childId = authUser.getUserId();
        Long walletId = walletService.getChildWallet(childId, authUser).getWalletId();

        PiggyBankDetailResponseDto response = piggyBankService.create(walletId, request);

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