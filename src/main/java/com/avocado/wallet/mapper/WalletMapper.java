package com.avocado.wallet.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface WalletMapper {

    // 사용자의 ID로 등록된 지갑의 정보를 조회
    Optional<Long> findWalletIdByUserId(@Param("userId") Long userId);

}
