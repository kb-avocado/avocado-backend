package com.avocado.domain.wallet.service;

import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface WalletService {

    /**
     * childId 기준으로 자녀 선불지갑 단건 정보를 조회한다.
     * 인증 사용자 확인, 자녀 존재 확인, 조회 권한 검증, 지갑 존재 확인을 순서대로 처리한다.
     */
    WalletResponseDto getChildWallet(
            Long childId,
            AuthUser authUser
    );

    /**
     * 자녀 회원 ID로 ACTIVE 상태의 선불지갑을 조회한다.
     *
     * @param childId 조회할 자녀 회원 ID
     * @return ACTIVE 상태의 선불지갑 정보
     * @throws com.avocado.global.exception.BusinessException ACTIVE 선불지갑이 존재하지 않는 경우
     */
    WalletVo getActiveWallet(
            Long childId
    );

    /**
     * 해당 선불지갑이 자녀 소유이며 ACTIVE 상태인지 검증한다.
     *
     * @param childId 선불지갑 소유 자녀 ID
     * @param walletId 확인할 선불지갑 ID
     */
    void validateActiveWallet(
            Long childId,
            Long walletId
    );

    /**
     * 부모 외부 계좌에서 들어온 금액을
     * 아이 선불지갑에 입금한다.
     *
     * 지갑 잠금 조회, 상태 확인, 잔액 증가,
     * 지갑 거래 이력과 원장 생성을 함께 처리한다.
     *
     * @param childId 아이 회원 ID
     * @param amount 입금 금액
     * @param traceId 연관 거래 추적 ID
     */
    void depositFromAccount(
            Long childId,
            Long amount,
            String traceId
    );

    /**
     * 저금통 저축을 위해
     * 아이 선불지갑에서 금액을 출금한다.
     *
     * @param childId 아이 회원 ID
     * @param amount 출금 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
     */
    void withdrawForPiggyBank(
            Long childId,
            Long amount,
            String traceId
    );

    /**
     * 해지된 저금통에서 반환된 금액을
     * 아이 선불지갑에 입금한다.
     *
     * @param childId 아이 회원 ID
     * @param amount 저금통에서 반환된 금액
     * @param traceId 저금통 거래와 연결하기 위한 추적 ID
     */
    void depositFromPiggyBank(
            Long childId,
            Long amount,
            String traceId
    );
}