// report/service/ReportService.java
package com.avocado.domain.report.service;

import com.avocado.domain.report.dto.response.ReportResponseDto;

public interface ReportService {
    ReportResponseDto getReport(String yearMonth, Long childId);
}