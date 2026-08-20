package com.avocado.domain.report.mapper;

import com.avocado.domain.report.domain.TopSpotRow;
import com.avocado.domain.report.dto.response.MonthlyComparisonItemDto;
import com.avocado.domain.report.dto.response.SavingsDto;
import com.avocado.domain.report.dto.response.SummaryDto;
import com.avocado.domain.report.dto.response.TopSpotDto;
import com.avocado.domain.report.dto.response.*;
import org.springframework.stereotype.Component;

import java.time.YearMonth;
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

    /*
     * 비중(percentage)은 그 달 전체 소비 금액 대비로 계산한다.
     */
    public List<TopSpotDto> toTopSpotDtos(List<TopSpotRow> rows, long totalSpent) {
        java.util.List<TopSpotDto> result = new java.util.ArrayList<>();

        int rank = 1;
        for (TopSpotRow row : rows) {
            int percentage = totalSpent <= 0 ? 0 : (int) Math.round(row.getAmount() * 100.0 / totalSpent);
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

    //저축률(이번달 저축액 / 이번 달 순수령 용돈)
    public SavingsDto toSavingsDto(long totalSaved, long allowanceReceived) {
        Integer savingsRate = allowanceReceived <= 0
                ? null
                : (int) Math.round(totalSaved * 100.0 / allowanceReceived);

        return SavingsDto.builder()
                .totalSaved(totalSaved)
                .savingsRate(savingsRate)
                .build();
    }
}