package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.SpendingReportType;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

@MapperScan
public interface SpendingReportTypeMapper {
    SpendingReportType findByCode(@Param("code") String code);

    SpendingReportType findById(@Param("id") Long id);
}