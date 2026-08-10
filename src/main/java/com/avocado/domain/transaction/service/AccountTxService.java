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
}
