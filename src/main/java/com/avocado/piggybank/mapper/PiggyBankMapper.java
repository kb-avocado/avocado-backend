package com.avocado.piggybank.mapper;

import com.avocado.piggybank.domain.BonusType;
import com.avocado.piggybank.domain.PiggyBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.avocado.piggybank.domain.PiggyBank;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PiggyBankMapper {

    PiggyBank selectById(@Param("id") Long id);

    int updateBonus(
            @Param("id") Long id,
            @Param("bonusType") BonusType bonusType,
            @Param("bonusValue") Long bonusValue
    );

    int increaseBalance(
            @Param("id") Long id,
            @Param("balance") Long balance,
            @Param("status") String status,
            @Param("firstDepositedAt") LocalDateTime firstDepositedAt,
            @Param("targetReachedAt") LocalDateTime targetReachedAt
    );

    // 지갑 + 상태(여러 개)로 저금통 목록 조회 (진행중/완료 탭 공용)
    List<PiggyBank> selectByWalletIdAndStatuses(
            @Param("walletId") Long walletId,
            @Param("statuses") List<String> statuses
    );
    //지갑 + 상태(여러 개)에 해당하는 저금통 개수 (진행중 개수 카운트용)
    int countByWalletIdAndStatuses(
            @Param("walletId") Long walletId,
            @Param("statuses") List<String> statuses
    );

    //저금통 생성 추가
    int insert(PiggyBank piggyBank);

    Long selectLastInsertId();

    //저금통 삭제 추가
    int cancel(@Param("id") Long id);

}