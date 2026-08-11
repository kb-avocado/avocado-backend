package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.MonthlySpentRow;
import com.avocado.domain.report.domain.TopSpotRow;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

@MapperScan
public interface ReportMapper {

    // 지갑 소유자(child) 확인용 - wallets 테이블에서 walletId 조회
    Long findWalletIdByChildId(@Param("childId") Long childId);

    // 해당 월 총 소비 금액 (ㅇ월 소비 금액에 표시할 데이터)
    Long sumSpentAmount(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);

    // 해당 월 결제 건수 (소비 건수 ㅇ건 에 표시할 데이터)
    Integer countTransactions(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);

    // 해당 월 가맹처 별 소비 Top5 (가맹처 TOP5에 표시할 데이터)
    List<TopSpotRow> findTopSpots(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth, @Param("limit") int limit);

    // 최근 N개월 월별 소비 합계 (월별 소비 비교 그래프용)
    List<MonthlySpentRow> findMonthlySpentList(@Param("walletId") Long walletId, @Param("months") List<String> yearMonths);

    // 해당 월 저금통 저축 합계
    Long sumSavedAmount(@Param("walletId") Long walletId, @Param("yearMonth") String yearMonth);

    // 해당 월 순수령 용돈 (TRANSFER_IN - TRANSFER_CANCLE_IN). 저축률 계산의 분모로 사용
    Long sumAllowanceReceived(@Param("walletId")Long walletId, @Param("yearMonth") String yearMonth);

    // 이 지갑의 최초 거래 월 (이전 달 화살표 활성화 판단용)
    String findEarliestTransactionMonth(@Param("walletId") Long walletId);
}