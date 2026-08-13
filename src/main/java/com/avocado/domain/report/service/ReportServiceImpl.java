// report/service/ReportServiceImpl.java
package com.avocado.domain.report.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.report.dto.response.*;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.report.domain.MonthlySpentRow;
import com.avocado.domain.report.domain.TopSpotRow;
import com.avocado.domain.report.mapper.ReportConverter;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int TOP_SPOTS_LIMIT = 5;
    private static final int MONTHLY_COMPARISON_SIZE = 3;

    private final ReportMapper reportMapper;
    private final ReportConverter reportConverter;
    private final WalletMapper walletMapper;
    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;

    @Override
    public ReportResponseDto getReport(String yearMonth, Long childId, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        Long walletId = reportMapper.findWalletIdByChildId(targetChildId);
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
        long allowanceReceived = reportMapper.sumAllowanceReceived(walletId, yearMonth);
        SavingsDto savings = reportConverter.toSavingsDto(totalSaved, allowanceReceived);

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

        // 이번 달 데이터는 아직 집계 중이라 보여주지 않음 -> "지난달"까지만 조회 가능
        YearMonth lastViewableMonth = YearMonth.now().minusMonths(1);
        boolean hasNext = targetMonth.isBefore(lastViewableMonth);

        return NavigationDto.builder()
                .hasPrevious(hasPrevious)
                .hasNext(hasNext)
                .build();
    }

    /*
     * 요청받은 childId와 로그인 사용자 정보를 바탕으로
     * 실제 조회 대상 childId를 결정하고 접근 권한을 검증한다.
     * News/Home 도메인의 resolveTargetChildId와 동일한 패턴이다.
     * - childId가 없으면: 로그인 사용자가 CHILD일 때만 본인 ID로 대체한다. (PARENT는 필수)
     * - childId가 있으면: 본인(CHILD) 또는 연결된 보호자(PARENT)인지 검증한다.
     */
    private Long resolveTargetChildId(Long childId, AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        Long targetChildId = childId;
        if (targetChildId == null) {
            if (!UserType.CHILD.equals(authUser.getUserType())) {
                throw new BusinessException(INVALID_REQUEST);
            }
            targetChildId = authUser.getUserId();
        }

        validateChildAccess(targetChildId, authUser);
        return targetChildId;
    }

    private void validateChildAccess(Long childId, AuthUser authUser) {
        if (!userMapper.existsChildById(childId)) {
            throw new BusinessException(CHILD_NOT_FOUND);
        }

        if (isChildOwner(childId, authUser)) {
            return;
        }

        if (isConnectedParent(childId, authUser)) {
            return;
        }

        throw new BusinessException(FORBIDDEN);
    }

    private boolean isChildOwner(Long childId, AuthUser authUser) {
        return UserType.CHILD.equals(authUser.getUserType())
                && childId.equals(authUser.getUserId());
    }

    private boolean isConnectedParent(Long childId, AuthUser authUser) {
        return UserType.PARENT.equals(authUser.getUserType())
                && familyRelationMapper.existsActiveRelation(authUser.getUserId(), childId);
    }
}