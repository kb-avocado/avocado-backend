package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.dto.response.WalletTxListItemResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WalletTxMapper {

    // 지갑에 대한 전체 거래 내역 수 반환
    long countByWalletId(@Param("walletId") Long walletId);

    // 지갑에 대한 전체 거래 내역 조회
    List<WalletTxListItemResponseDto> findAllByWalletId(
            @Param("walletId") Long walletId,
            @Param("offset") int offset,
            @Param("size") int size
    );


}
