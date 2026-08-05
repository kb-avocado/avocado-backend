// report/service/ReportService.java
package com.avocado.report.service;

import com.avocado.report.dto.response.ReportResponseDto;

public interface ReportService {
    ReportResponseDto getReport(String yearMonth, Long childId);
}