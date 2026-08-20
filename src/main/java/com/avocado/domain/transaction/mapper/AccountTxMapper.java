package com.avocado.domain.transaction.mapper;

import org.apache.ibatis.annotations.Param;

public interface AccountTxMapper {
    /**
     * 부모 외부 계좌에서 아이 선불지갑으로 송금한
     * 계좌 거래 이력을 생성한다.
     *
     * @param accountId 외부 연동 계좌 ID
     * @param traceId   연관 거래 추적 ID
     * @param amount    거래 금액
     * @return INSERT된 행 수
     */
    int insertWalletChargeHistory(
            @Param("accountId") Long accountId,
            @Param("traceId") String traceId,
            @Param("amount") Long amount
    );

    /**
     * 아이 선불지갑에서 서비스 등록 계좌로 들어온 입금 이력을 생성한다.
     *
     * @param accountId 입금받는 계좌 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 입금 금액
     * @return 생성된 행 수
     */
    int insertWalletDepositHistory(
            @Param("accountId") Long accountId,
            @Param("traceId") String traceId,
            @Param("amount") Long amount
    );
}
