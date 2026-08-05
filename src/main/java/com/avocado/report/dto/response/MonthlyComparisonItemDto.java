// report/dto/response/MonthlyComparisonItemDto.java
package com.avocado.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MonthlyComparisonItemDto {
    private String yearMonth; // "2026-06"
    private String month;     // "6월" - 프론트 표시용
    private Long amount;
}