// report/service/ReportService.java
package com.avocado.domain.report.service;

import com.avocado.domain.report.dto.response.ReportResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface ReportService {
    ReportResponseDto getReport(String yearMonth, Long childId, AuthUser authUser);
}