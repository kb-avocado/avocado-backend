package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface WalletTxMapper {

    /**
     * 선불지갑의 전체 거래 건수를 조회한다.
     */
    long countByWalletId(
            @Param("walletId") Long walletId
    );

    /**
     * 선불지갑의 거래 목록을 페이지 단위로 조회한다.
     */
    List<WalletTxItemResponseDto> findAllByWalletId(
            @Param("walletId") Long walletId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 선불지갑의 특정 거래 상세 정보를 조회한다.
     *
     * walletId를 함께 조건으로 사용하여
     * 다른 사용자의 거래를 조회하지 못하도록 한다.
     */
    Optional<WalletTxDetailResponseDto> findDetailByWalletIdAndTransactionId(
            @Param("walletId") Long walletId,
            @Param("transactionId") Long transactionId
    );


}
