package com.avocado.domain.report.scheduler;

import com.avocado.domain.report.service.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.ZoneId;

// 매월 1일, 전달 리포트를 모든 자녀 대상으로 계산해서 저장하는 스케줄러
@Component
@RequiredArgsConstructor
public class ReportScheduler {

    // 서버가 어느 지역에서 뜨든 한국 시간을 기준으로 돈다.
    private static final String KST_ZONE = "Asia/Seoul";
    private static final ZoneId KST = ZoneId.of(KST_ZONE);

    private final ReportGenerationService reportGenerationService;

    /*
     * 매월 1일 새벽 1시(한국 기준) 실행. (초 분 시 일 월 요일)
     *
     * 조언을 채우는 AI 배치가 새벽 2시에 도므로 그전에 집계가 끝나 있어야 한다.
     * 대상 월도 KST로 구한다. JVM 기본 시간대(배포 환경은 보통 UTC)로 구하면
     * 1일 새벽에는 아직 전달로 읽혀 한 달 어긋난 리포트를 만든다.
     */
    @Scheduled(cron = "0 0 1 1 * *", zone = KST_ZONE)
    public void generatePreviousMonthReports() {
        YearMonth previousMonth = YearMonth.now(KST).minusMonths(1);
        reportGenerationService.generateForAllChildren(previousMonth);
    }
}