package com.avocado.domain.family.mapper;

import org.apache.ibatis.annotations.Param;

/**
 * 가족 연결이 확정될 때 아이에게 선불지갑을 만들어 주기 위한 매퍼.
 */
public interface FamilyWalletMapper {

    // 아이 한 명당 지갑 하나. 이미 있으면 만들지 않는다.
    boolean existsByChildId(@Param("childId") Long childId);

    void insertWallet(
            @Param("childId") Long childId,
            @Param("walletNumber") String walletNumber
    );
}
