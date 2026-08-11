package com.avocado.domain.report.service;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;

//소비리포트분류

public interface SpendingReportClassificationService {
    SpendingReportTypeDto classifyAndSave(String yearMonth, Long childId);


}