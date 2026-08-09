package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WalletTxMapper {

    // 해당 지갑의 전체 거래 수를 조회
    long countByWalletId(
            @Param("walletId") Long walletId
    );

    // 해당 지갑 거래내역 최신순 조회
    List<WalletTxItemResponseDto> findAllByWalletId(
            @Param("walletId") Long walletId,
            @Param("offset") int offset,
            @Param("size") int size
    );


}
