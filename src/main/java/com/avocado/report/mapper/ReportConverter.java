package com.avocado.report.mapper;

import com.avocado.report.domain.MonthlySpentRow;
import com.avocado.report.domain.TopSpotRow;
import com.avocado.report.dto.response.*;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ReportConverter {

    public SummaryDto toSummaryDto(long totalSpent, long comparedToLastMonth, int transactionCount) {
        return SummaryDto.builder()
                .totalSpent(totalSpent)
                .comparedToLastMonth(comparedToLastMonth)
                .transactionCount(transactionCount)
                .build();
    }

    public List<TopSpotDto> toTopSpotDtos(List<TopSpotRow> rows) {
        long total = rows.stream().mapToLong(TopSpotRow::getAmount).sum();
        java.util.List<TopSpotDto> result = new java.util.ArrayList<>();

        int rank = 1;
        for (TopSpotRow row : rows) {
            int percentage = total == 0 ? 0 : (int) Math.round(row.getAmount() * 100.0 / total);
            result.add(TopSpotDto.builder()
                    .rank(rank++)
                    .category(row.getCategory())
                    .amount(row.getAmount())
                    .percentage(percentage)
                    .build());
        }
        return result;
    }

    public MonthlyComparisonItemDto toMonthlyComparisonItem(String yearMonth, Long amount) {
        YearMonth ym = YearMonth.parse(yearMonth);
        return MonthlyComparisonItemDto.builder()
                .yearMonth(yearMonth)
                .month(ym.getMonthValue() + "월")
                .amount(amount == null ? 0L : amount)
                .build();
    }

    public SavingsDto toSavingsDto(long totalSaved) {
        return SavingsDto.builder()
                .totalSaved(totalSaved)
                .savingsRate(null) // TODO: 용돈 지급 테이블 확정되면 계산 로직 추가
                .build();
    }
}