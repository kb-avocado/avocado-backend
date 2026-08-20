// report/service/ReportServiceImpl.java
package com.avocado.domain.report.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.report.domain.ChildSpendingReport;
import com.avocado.domain.report.dto.response.*;
import com.avocado.domain.report.mapper.ChildSpendingReportMapper;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.report.mapper.ReportConverter;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final int MONTHLY_COMPARISON_SIZE = 3;

    private final ReportMapper reportMapper;
    private final ReportConverter reportConverter;
    private final ChildSpendingReportMapper childSpendingReportMapper;
    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ReportResponseDto getReport(String yearMonth, Long childId, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        YearMonth targetMonth = YearMonth.parse(yearMonth);

        ChildSpendingReport report = findReport(targetChildId, targetMonth);
        if (report == null) {
            throw new BusinessException(ErrorCode.REPORT_NOT_READY);
        }

        YearMonth previousMonth = targetMonth.minusMonths(1);
        ChildSpendingReport previousReport = findReport(targetChildId, previousMonth);
        long lastMonthSpent = previousReport == null ? 0 : previousReport.getTotalSpent();

        SummaryDto summary = reportConverter.toSummaryDto(
                report.getTotalSpent(),
                report.getTotalSpent() - lastMonthSpent,
                report.getTransactionCount()
        );

        List<TopSpotDto> topSpots = parseTopSpots(report.getTopSpots());

        List<String> recentMonths = buildRecentMonths(targetMonth, MONTHLY_COMPARISON_SIZE);
        List<MonthlyComparisonItemDto> monthlyComparison = new ArrayList<>();
        for (String ym : recentMonths) {
            ChildSpendingReport r = ym.equals(yearMonth) ? report : findReport(targetChildId, YearMonth.parse(ym));
            monthlyComparison.add(reportConverter.toMonthlyComparisonItem(
                    ym, r == null ? null : r.getTotalSpent()));
        }

        SavingsDto savings = SavingsDto.builder()
                .totalSaved(report.getTotalSaved())
                .savingsRate(report.getSavingRate() == null ? null : report.getSavingRate().intValue())
                .build();

        NavigationDto navigation = buildNavigation(targetChildId, targetMonth);

        return ReportResponseDto.builder()
                .yearMonth(yearMonth)
                .summary(summary)
                .topSpots(topSpots)
                .monthlyComparison(monthlyComparison)
                .savings(savings)
                .navigation(navigation)
                .advice(resolveAdvice(report, authUser))
                .build();
    }

    /*
     * 보는 사람에 맞는 조언을 고른다.
     *
     * 화면에 들어갈 자리는 하나뿐이라, 아이용·보호자용 중 어느 쪽을 쓸지는 여기서 정해
     * 프론트가 분기하지 않도록 한다. AI 배치 전이면 두 값 모두 null이다.
     */
    private String resolveAdvice(ChildSpendingReport report, AuthUser authUser) {
        return UserType.PARENT.equals(authUser.getUserType())
                ? report.getParentAdvice()
                : report.getChildAdvice();
    }

    private ChildSpendingReport findReport(Long childId, YearMonth month) {
        return childSpendingReportMapper.findByChildAndYearMonth(childId, month.getYear(), month.getMonthValue());
    }

    private List<TopSpotDto> parseTopSpots(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<TopSpotDto>>() {});
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    private List<String> buildRecentMonths(YearMonth targetMonth, int size) {
        List<String> months = new ArrayList<>();
        for (int i = size - 1; i >= 0; i--) {
            months.add(targetMonth.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
        return months;
    }

    private NavigationDto buildNavigation(Long childId, YearMonth targetMonth) {
        YearMonth previousMonth = targetMonth.minusMonths(1);
        boolean hasPrevious = findReport(childId, previousMonth) != null;

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