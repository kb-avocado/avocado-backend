package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferResultVo;
import com.avocado.domain.transfer.dto.request.AccountToWalletTransferRequestDto;
import com.avocado.domain.transfer.dto.request.WalletTransferRequestDto;
import com.avocado.domain.transfer.dto.response.AccountToWalletTransferResponseDto;
import com.avocado.domain.transfer.dto.response.WalletTransferResponseDto;

public interface TransferService {

    /**
     * 외부 API 요청을 받아 부모 계좌에서 자녀 선불지갑으로 금액을 송금한다.
     *
     * @param requestDto 부모 계좌-자녀 선불지갑 송금 요청 정보
     * @return 송금 상대방과 송금 금액 정보
     */
    AccountToWalletTransferResponseDto transferAccountToWallet(
            Long parentId,
            AccountToWalletTransferRequestDto requestDto
    );

    /**
     * 다른 서비스에서 부모 계좌-자녀 선불지갑 송금을 호출할 때 사용한다.
     *
     * @param parentId 송금하는 부모 회원 ID
     * @param childId 송금받는 자녀 회원 ID
     * @param amount 송금 금액
     * @param traceId 연관 거래를 식별하기 위한 추적 ID
     */
    void transferAccountToWallet(
            Long parentId,
            Long childId,
            Long amount,
            String traceId
    );

    /**
     * 저금통 저축을 위해 자녀 선불지갑에서 금액을 출금한다.
     *
     * @param childId 출금 대상 자녀 회원 ID
     * @param amount 저금통으로 보낼 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
     */
    void transferWalletToPiggyBank(
            Long childId,
            Long amount,
            String traceId
    );

    /**
     * 저금통에서 출금된 금액을 자녀 선불지갑에 입금한다.
     *
     * @param childId 입금 대상 자녀 회원 ID
     * @param amount 저금통에서 반환된 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
     */
    void transferPiggyBankToWallet(
            Long childId,
            Long amount,
            String traceId
    );

    /**
     * 아이 선불지갑에서 내부 선불지갑 또는 외부 계좌로 송금한다.
     *
     * @param childId 송금하는 아이 회원 ID
     * @param requestDto 송금 요청 정보
     * @return 송금 처리 결과
     */
    WalletTransferResponseDto transferFromWallet(
            Long childId,
            WalletTransferRequestDto requestDto
    );
}
