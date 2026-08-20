package com.avocado.domain.piggybank.controller;

import com.avocado.domain.wallet.service.WalletService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import com.avocado.domain.piggybank.service.PiggyBankCheerMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import javax.validation.Valid;
import java.util.List;

import static com.avocado.global.response.code.SuccessCode.*;

@RestController
@RequestMapping("/api/piggybanks/{piggyBankId}/cheer-messages")
@RequiredArgsConstructor
@Api(tags = "저금통 응원 메시지 API")
public class PiggyBankCheerMessageController {

    private final PiggyBankCheerMessageService piggyBankCheerMessageService;
    private final WalletService walletService;

    @PostMapping
    @ApiOperation(value = "응원 메시지 전송", notes = "보호자가 저금통에 응원 메시지를 남깁니다.")
    public ResponseEntity<ApiResponse<PiggyBankCheerMessageResponseDto>> sendMessage(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankCheerMessageCreateRequestDto request,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long targetChildId = (childId != null) ? childId : authUser.getUserId();
        Long walletId = walletService.getChildWallet(targetChildId, authUser).getWalletId();

        PiggyBankCheerMessageResponseDto response =
                piggyBankCheerMessageService.sendMessage(piggyBankId, request, authUser, walletId);

        return ResponseEntity
                .status(CHEER_MESSAGE_SENT.getHttpStatus())
                .body(ApiResponse.success(CHEER_MESSAGE_SENT, response));
    }

    @GetMapping
    @ApiOperation(value = "응원 메시지 조회", notes = "저금통에 남겨진 응원 메시지 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<PiggyBankCheerMessageResponseDto>>> getMessages(
            @PathVariable Long piggyBankId,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
        Long targetChildId = (childId != null) ? childId : authUser.getUserId();
        Long walletId = walletService.getChildWallet(targetChildId, authUser).getWalletId();

        List<PiggyBankCheerMessageResponseDto> messages =
                piggyBankCheerMessageService.getMessages(piggyBankId, walletId);

        return ResponseEntity
                .status(CHEER_MESSAGE_FETCHED.getHttpStatus())
                .body(ApiResponse.success(CHEER_MESSAGE_FETCHED, messages));
    }

    @DeleteMapping("/{messageId}")
    @ApiOperation(value = "응원 메시지 삭제", notes = "보호자가 본인이 작성한 응원 메시지를 삭제합니다.")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long piggyBankId,
            @PathVariable Long messageId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        piggyBankCheerMessageService.deleteMessage(piggyBankId, messageId, authUser);

        return ResponseEntity
                .status(CHEER_MESSAGE_DELETED.getHttpStatus())
                .body(ApiResponse.success(CHEER_MESSAGE_DELETED));
    }
}