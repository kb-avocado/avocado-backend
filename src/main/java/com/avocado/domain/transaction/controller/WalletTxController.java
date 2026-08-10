package com.avocado.domain.transaction.controller;

import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.domain.transaction.service.WalletTxService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.WALLET_TX_DETAIL_FETCHED;
import static com.avocado.global.response.code.SuccessCode.WALLET_TX_LIST_FETCHED;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wallets")
public class WalletTxController {

    private final WalletTxService walletTxService;


    /**
     * 로그인한 아이의 선불지갑 거래 내역을 조회한다.
     *
     * @param requestDto 페이지 조회 조건
     * @return 페이지네이션이 적용된 선불지갑 거래 내역
     */
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<PageResponse<WalletTxItemResponseDto>>> getWalletTxList(
            @Valid @ModelAttribute WalletTxListRequestDto requestDto
    ) {
        // TODO: 임시 회원 아이디
        Long userId = 102L;

        // 회원의 선불지갑 전체 거래 목록을 조회
        PageResponse<WalletTxItemResponseDto> response = walletTxService.getWalletTxList(
                userId,
                requestDto
        );

        return ResponseEntity
                .status(WALLET_TX_LIST_FETCHED.getHttpStatus())
                .body(ApiResponse.success(WALLET_TX_LIST_FETCHED, response));
    }

    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<ApiResponse<WalletTxDetailResponseDto>> getWalletTxDetail(
            @PathVariable Long transactionId
    ) {
        // TODO: 임시 회원 아이디
        Long userId = 102L;

        WalletTxDetailResponseDto response = walletTxService.getWalletTxDetail(
                userId,
                transactionId
        );

        return ResponseEntity
                .status(WALLET_TX_DETAIL_FETCHED.getHttpStatus())
                .body(ApiResponse.success(WALLET_TX_DETAIL_FETCHED, response));
    }

}
