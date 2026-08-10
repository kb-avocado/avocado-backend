package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.ChildSpendingReport;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ChildSpendingReportMapper {
    // 같은 아이-같은 달 조합이면 유형만 갱신 (uk_child_report_month 활용)
    void upsert(ChildSpendingReport report);
}