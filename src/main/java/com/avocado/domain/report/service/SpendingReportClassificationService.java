package com.avocado.domain.report.service;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;

public interface SpendingReportClassificationService {
    SpendingReportTypeDto classifyAndSave(String yearMonth, Long childId);
}