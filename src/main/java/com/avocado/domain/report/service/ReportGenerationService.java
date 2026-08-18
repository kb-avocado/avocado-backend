package com.avocado.domain.report.service;

import java.time.YearMonth;

public interface ReportGenerationService {

    // 대상 월(targetMonth)의 리포트를 지갑을 가진 모든 자녀에 대해 계산하여 저장한다.
    void generateForAllChildren(YearMonth targetMonth);
}