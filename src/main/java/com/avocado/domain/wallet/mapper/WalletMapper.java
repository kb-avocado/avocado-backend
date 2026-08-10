package com.avocado.domain.wallet.mapper;

import com.avocado.domain.wallet.domain.WalletVo;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface WalletMapper {

    // 사용자의 ID로 등록된 지갑 ID를 조회한다.
    Optional<Long> findWalletIdByUserId(
            @Param("userId") Long userId
    );

    // 조회 대상 자녀 회원이 존재하는지 확인한다.
    boolean existsChildById(
            @Param("childId") Long childId
    );

    // 보호자와 자녀의 활성 가족 관계가 존재하는지 확인한다.
    boolean existsActiveFamilyRelation(
            @Param("parentId") Long parentId,
            @Param("childId") Long childId
    );

    // 자녀 ID로 선불지갑 단건 정보를 조회한다.
    Optional<WalletVo> findByChildId(
            @Param("childId") Long childId
    );
}
