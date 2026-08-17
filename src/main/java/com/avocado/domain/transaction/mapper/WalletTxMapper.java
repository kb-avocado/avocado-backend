package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface WalletTxMapper {

    // 선불지갑의 전체 거래 건수를 조회
    long countByWalletId(
            @Param("walletId") Long walletId
    );

    // 선불지갑의 거래 목록을 페이지 단위로 조회
    List<WalletTxItemResponseDto> findAllByWalletId(
            @Param("walletId") Long walletId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    // 선불지갑의 특정 거래 상세 정보를 조회
    Optional<WalletTxDetailResponseDto> findDetailByWalletIdAndTransactionId(
            @Param("walletId") Long walletId,
            @Param("transactionId") Long transactionId
    );

    // 선불지갑 거래 이력을 생성
    int insertWalletHistory(
            WalletHistoryVo transaction
    );

    // 선불지갑 잔액 변화를 원장에 기록
    int insertWalletLedger(
            @Param("historyId") Long historyId,
            @Param("walletId") Long walletId,
            @Param("ledgerType") String ledgerType,
            @Param("amount") Long amount,
            @Param("balanceBefore") Long balanceBefore,
            @Param("balanceAfter") Long balanceAfter
    );
}
