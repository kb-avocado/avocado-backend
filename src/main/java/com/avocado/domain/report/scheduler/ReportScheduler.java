package com.avocado.domain.report.scheduler;

import com.avocado.domain.report.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

// 매월 1일, 전달 리포트를 모든 자녀 대상으로 계산해서 저장하는 스케줄러
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    private final ReportGenerationService reportGenerationService;

    // 매월 1일 01:00 실행 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 1 1 * *")
    public void generatePreviousMonthReports() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        reportGenerationService.generateForAllChildren(previousMonth);
    }
}