package com.avocado.domain.transaction.service;

public interface AccountTxService {
    /**
     * 부모 외부 계좌에서 아이 선불지갑으로 송금한
     * 계좌 거래 이력을 기록한다.
     *
     * @param accountId 부모 외부 계좌 ID
     * @param traceId   연관 거래 추적 ID
     * @param amount    거래 금액
     */
    void recordWalletCharge(
            Long accountId,
            String traceId,
            Long amount
    );

    /**
     * 아이 선불지갑에서 서비스 등록 계좌로 송금된 입금 이력을 기록한다.
     *
     * @param accountId 입금받는 부모 연동 계좌 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 입금 금액
     */
    void recordWalletDeposit(
            Long accountId,
            String traceId,
            Long amount
    );
}
