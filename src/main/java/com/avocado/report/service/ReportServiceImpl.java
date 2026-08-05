// report/service/ReportServiceImpl.java
package com.avocado.report.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.response.code.ErrorCode;
import com.avocado.report.domain.MonthlySpentRow;
import com.avocado.report.domain.TopSpotRow;
import com.avocado.report.dto.response.*;
import com.avocado.report.mapper.ReportConverter;
import com.avocado.report.mapper.ReportMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int TOP_SPOTS_LIMIT = 5;
    private static final int MONTHLY_COMPARISON_SIZE = 3;

    private final ReportMapper reportMapper;
    private final ReportConverter reportConverter;

    @Override
    public ReportResponseDto getReport(String yearMonth, Long childId) {
        Long walletId = reportMapper.findWalletIdByChildId(childId);
        if (walletId == null) {
            throw new BusinessException(ErrorCode.WALLET_NOT_FOUND);
        }

        YearMonth targetMonth = YearMonth.parse(yearMonth);
        String previousMonth = targetMonth.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        long totalSpent = reportMapper.sumSpentAmount(walletId, yearMonth);
        long lastMonthSpent = reportMapper.sumSpentAmount(walletId, previousMonth);
        int transactionCount = reportMapper.countTransactions(walletId, yearMonth);

        SummaryDto summary = reportConverter.toSummaryDto(
                totalSpent,
                totalSpent - lastMonthSpent,
                transactionCount
        );

        List<TopSpotRow> topSpotRows = reportMapper.findTopSpots(walletId, yearMonth, TOP_SPOTS_LIMIT);
        List<TopSpotDto> topSpots = reportConverter.toTopSpotDtos(topSpotRows);

        List<String> recentMonths = buildRecentMonths(targetMonth, MONTHLY_COMPARISON_SIZE);
        List<MonthlySpentRow> monthlySpentRows = reportMapper.findMonthlySpentList(walletId, recentMonths);
        Map<String, Long> monthlySpentMap = monthlySpentRows.stream()
                .collect(Collectors.toMap(MonthlySpentRow::getYearMonth, MonthlySpentRow::getAmount));

        List<MonthlyComparisonItemDto> monthlyComparison = new ArrayList<>();
        for (String ym : recentMonths) {
            monthlyComparison.add(reportConverter.toMonthlyComparisonItem(ym, monthlySpentMap.get(ym)));
        }

        long totalSaved = reportMapper.sumSavedAmount(walletId, yearMonth);
        SavingsDto savings = reportConverter.toSavingsDto(totalSaved);

        NavigationDto navigation = buildNavigation(walletId, targetMonth);

        return ReportResponseDto.builder()
                .yearMonth(yearMonth)
                .summary(summary)
                .topSpots(topSpots)
                .monthlyComparison(monthlyComparison)
                .savings(savings)
                .navigation(navigation)
                .build();
    }

    private List<String> buildRecentMonths(YearMonth targetMonth, int size) {
        List<String> months = new ArrayList<>();
        for (int i = size - 1; i >= 0; i--) {
            months.add(targetMonth.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return months;
    }

    private NavigationDto buildNavigation(Long walletId, YearMonth targetMonth) {
        String earliestMonth = reportMapper.findEarliestTransactionMonth(walletId);
        boolean hasPrevious = earliestMonth != null
                && !targetMonth.minusMonths(1).isBefore(YearMonth.parse(earliestMonth));
        boolean hasNext = targetMonth.isBefore(YearMonth.now());

        return NavigationDto.builder()
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .build();
    }
}