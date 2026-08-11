package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.ChildSpendingReport;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan
public interface ChildSpendingReportMapper {

    //해당 아이-해당 달에 이미 계산된 기록이 있는지 확인, 있으면 재계산하지 않고 그대로 반환한다
    ChildSpendingReport findByChildAndYearMonth(
            @Param("childId") Long childId,
            @Param("year") int year,
            @Param("month") int month
    );

    // 같은 아이-같은 달 조합이면 유형만 갱신 (uk_child_report_month 활용)
    void upsert(ChildSpendingReport report);
}