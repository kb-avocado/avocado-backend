package com.avocado.domain.wallet.mapper;

import com.avocado.domain.wallet.domain.WalletVo;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface WalletMapper {

    // 사용자의 ID로 등록된 지갑 ID를 조회
    Optional<Long> findWalletIdByUserId(
            @Param("userId") Long userId
    );

    // 자녀 ID로 선불지갑 단건 정보를 조회
    Optional<WalletVo> findByChildId(
            @Param("childId") Long childId
    );

    // 선불지갑 잔액을 증가
    int increaseBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Long amount
    );

    // 선불지갑 잔액을 감소
    int decreaseBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Long amount
    );


    // 아이 소유 지갑을 잠금 조회
    Optional<WalletVo> findForUpdateByIdAndChildId(
            @Param("walletId") Long walletId,
            @Param("childId") Long childId
    );

    // 아이 소유 지갑을 childId 기준으로 잠금 조회
    Optional<WalletVo> findForUpdateByChildId(
            @Param("childId") Long childId
    );
}
