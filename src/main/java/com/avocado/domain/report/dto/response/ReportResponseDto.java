// report/dto/response/ReportResponseDto.java
package com.avocado.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReportResponseDto {
    private String yearMonth;
    private SummaryDto summary;
    private List<TopSpotDto> topSpots;
    private List<MonthlyComparisonItemDto> monthlyComparison;
    private SavingsDto savings;
    private NavigationDto navigation;

    // AI가 채우는 월간 조언. 요청한 사람이 아이면 아이용, 보호자면 보호자용 문구가 담긴다.
    // 아직 생성되지 않았으면 null이다.
    private String advice;
}