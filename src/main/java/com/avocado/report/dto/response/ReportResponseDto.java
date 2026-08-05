// report/dto/response/ReportResponseDto.java
package com.avocado.report.dto.response;

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
}