package com.avocado.domain.wallet.service;

import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface WalletService {
    /*
     * childId 기준으로 자녀 선불지갑 단건 정보를 조회한다.
     * 인증 사용자 확인, 자녀 존재 확인, 조회 권한 검증, 지갑 존재 확인을 순서대로 처리한다.
     */
    WalletResponseDto getChildWallet(
            Long childId,
            AuthUser authUser
    );

    /**
     * 부모 외부 계좌에서 들어온 금액을
     * 아이 선불지갑에 입금한다.
     *
     * 지갑 잠금 조회, 상태 확인, 잔액 증가,
     * 지갑 거래 이력과 원장 생성을 함께 처리한다.
     *
     * @param childId 아이 회원 ID
     * @param walletId 선불지갑 ID
     * @param amount 입금 금액
     * @param traceId 연관 거래 추적 ID
     */
    void depositFromAccount(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    );

    /**
     * 저금통 저축을 위해
     * 아이 선불지갑에서 금액을 출금한다.
     *
     * @param childId 아이 회원 ID
     * @param walletId 선불지갑 ID
     * @param amount 출금 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
     */
    void withdrawForPiggyBank(
            Long childId,
            Long walletId,
            Long amount,
            String traceId
    );
}
