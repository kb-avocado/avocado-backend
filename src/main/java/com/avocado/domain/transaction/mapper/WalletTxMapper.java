package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface WalletTxMapper {

    // 선불지갑의 전체 거래 건수를 조회한다.
    long countByWalletId(
            @Param("walletId") Long walletId
    );

    // 선불지갑의 거래 목록을 페이지 단위로 조회한다.
    List<WalletTxItemResponseDto> findAllByWalletId(
            @Param("walletId") Long walletId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 선불지갑의 특정 거래 상세 정보를 조회한다.
    Optional<WalletTxDetailResponseDto> findDetailByWalletIdAndTransactionId(
            @Param("walletId") Long walletId,
            @Param("transactionId") Long transactionId
    );

    // 아이 지갑 히스토리에 부모 계좌 충전을 CHARGE 거래로 기록한다.
    int insertWalletHistory(
            WalletHistoryVo transaction
    );

    // 실제 지갑 잔액 증가를 원장에 기록한다.
    int insertWalletLedger(
            @Param("historyId") Long historyId,
            @Param("walletId") Long walletId,
            @Param("ledgerType") String ledgerType,
            @Param("amount") Long amount,
            @Param("balanceBefore") Long balanceBefore,
            @Param("balanceAfter") Long balanceAfter
    );
}
