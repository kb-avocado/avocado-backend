package com.avocado.domain.report.mapper;

import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan
public interface SpendingClassificationMapper {

    // 이번 달 소비가 발생한 날짜 수 (FREQUENT_SPARROW 판정용)
    Integer findDistinctSpendingDayCount(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);

    // 이번 달 가장 많이 쓴 단일 가맹점의 합계 금액 (ONE_STORE_SNIPER 판정용)
    Long findTopMerchantAmount(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);

    // 이번 달 목표 최종 달성한 저금통 수 (SAVING_DREAMER 판정용)
    Integer countAchievedPiggyBanks(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);
}