package com.avocado.domain.transaction.service;

import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.global.response.PageResponse;
import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;

public interface WalletTxService {
    /**
     * 회원의 선불지갑 거래 내역을 페이지 단위로 조회한다.
     *
     * @param userId     로그인한 회원 ID
     * @param requestDto 페이지 조회 조건
     * @return 페이지네이션이 적용된 선불지갑 거래 목록
     */
    PageResponse<WalletTxItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto requestDto
    );

    /**
     * 회원의 특정 선불지갑 거래 상세 정보를 조회한다.
     *
     * @param userId        로그인한 회원 ID
     * @param transactionId 조회할 거래 ID
     * @return 선불지갑 거래 상세 정보
     */
    WalletTxDetailResponseDto getWalletTxDetail(
            Long userId,
            Long transactionId
    );

    /**
     * 내부 선불지갑 송금의 출금 거래와 원장을 기록한다.
     *
     * @param walletId 송금자 지갑 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 송금 금액
     * @param counterpartyWalletId 수취 지갑 ID
     * @param counterpartyName 수취인 이름
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     */
    void recordTransferOutToWallet(
            Long walletId,
            String traceId,
            Long amount,
            Long counterpartyWalletId,
            String counterpartyName,
            Long balanceBefore,
            Long balanceAfter
    );

    /**
     * 내부 선불지갑 송금의 입금 거래와 원장을 기록한다.
     *
     * @param walletId 수취 지갑 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 송금 금액
     * @param counterpartyWalletId 송금자 지갑 ID
     * @param counterpartyName 송금자 이름
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     */
    void recordTransferInFromWallet(
            Long walletId,
            String traceId,
            Long amount,
            Long counterpartyWalletId,
            String counterpartyName,
            Long balanceBefore,
            Long balanceAfter
    );

    /**
     * 외부 계좌 송금의 출금 거래와 원장을 기록한다.
     *
     * @param walletId 송금자 지갑 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 송금 금액
     * @param bankCode 수취 금융기관 코드
     * @param accountNumber 수취 계좌번호
     * @param recipientName 수취인 이름
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     */
    void recordTransferOutToAccount(
            Long walletId,
            String traceId,
            Long amount,
            String bankCode,
            String accountNumber,
            String recipientName,
            Long balanceBefore,
            Long balanceAfter
    );
}
