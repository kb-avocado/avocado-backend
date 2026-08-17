package com.avocado.domain.report.service;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
import com.avocado.global.security.jwt.dto.AuthUser;

//소비리포트분류

public interface SpendingReportClassificationService {
    SpendingReportTypeDto classifyAndSave(String yearMonth, Long childId, AuthUser authUser);


}