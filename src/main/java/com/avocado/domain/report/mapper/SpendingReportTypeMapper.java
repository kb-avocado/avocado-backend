package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.SpendingReportType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SpendingReportTypeMapper {
    SpendingReportType findByCode(@Param("code") String code);
}