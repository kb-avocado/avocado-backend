package com.avocado.domain.report.service;

import com.avocado.domain.report.domain.ChildSpendingReport;
import com.avocado.domain.report.domain.SpendingReportType;
import com.avocado.domain.report.domain.TopSpotRow;
import com.avocado.domain.report.dto.response.TopSpotDto;
import com.avocado.domain.report.mapper.ChildSpendingReportMapper;
import com.avocado.domain.report.mapper.ReportConverter;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.domain.report.mapper.SpendingClassificationMapper;
import com.avocado.domain.report.mapper.SpendingReportTypeMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.user.mapper.UserMapper;

@Service
@RequiredArgsConstructor
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final int TOP_SPOTS_LIMIT = 5;

    // 소비 유형 판정 기준값 (SpendingReportClassificationServiceImpl과 동일)
    private static final int SAVING_DREAMER_MIN_ACHIEVED = 2;
    private static final int FREQUENT_SPARROW_MIN_DAYS = 25;
    private static final double BIG_SPENDER_MIN_AVG_AMOUNT = 15000;
    private static final int BIG_SPENDER_MAX_COUNT = 5;
    private static final double ROLLER_COASTER_RATE = 0.5;
    private static final int CAREFUL_OWL_MAX_COUNT = 10;
    private static final double ONE_STORE_SNIPER_SHARE = 0.5;
    private static final long SMALL_SAVER_MAX_AMOUNT = 10000;

    private final ReportMapper reportMapper;
    private final ReportConverter reportConverter;
    private final SpendingClassificationMapper spendingClassificationMapper;
    private final SpendingReportTypeMapper spendingReportTypeMapper;
    private final ChildSpendingReportMapper childSpendingReportMapper;
    private final NotificationService notificationService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public void generateForAllChildren(YearMonth targetMonth) {
        List<Long> childIds = reportMapper.findAllActiveChildIds();
        for (Long childId : childIds) {
            generateForChild(childId, targetMonth);
        }
    }

    @Override
    @Transactional
    public void generateForChild(Long childId, YearMonth targetMonth) {
        Long walletId = reportMapper.findWalletIdByChildId(childId);
        if (walletId == null) {
            return; // 지갑 없는 아이는 건너뜀
        }

        String yearMonth = targetMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String previousMonth = targetMonth.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        long totalSpent = reportMapper.sumSpentAmount(walletId, yearMonth);
        long lastMonthSpent = reportMapper.sumSpentAmount(walletId, previousMonth);
        int transactionCount = reportMapper.countTransactions(walletId, yearMonth);

        List<TopSpotRow> topSpotRows = reportMapper.findTopSpots(walletId, yearMonth, TOP_SPOTS_LIMIT);
        List<TopSpotDto> topSpots = reportConverter.toTopSpotDtos(topSpotRows);

        long totalSaved = reportMapper.sumSavedAmount(walletId, yearMonth);
        long allowanceReceived = reportMapper.sumAllowanceReceived(walletId, yearMonth);
        BigDecimal savingRate = allowanceReceived <= 0
                ? BigDecimal.ZERO
                : BigDecimal.valueOf(totalSaved * 100.0 / allowanceReceived).setScale(2, RoundingMode.HALF_UP);

        String code = classify(walletId, yearMonth, totalSpent, lastMonthSpent, transactionCount);
        SpendingReportType type = spendingReportTypeMapper.findByCode(code);
        if (type == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        ChildSpendingReport report = new ChildSpendingReport();
        report.setChildId(childId);
        report.setReportTypeId(type.getId());
        report.setReportYear(targetMonth.getYear());
        report.setReportMonth(targetMonth.getMonthValue());
        report.setTotalSpent(totalSpent);
        report.setTransactionCount(transactionCount);
        report.setTopSpots(toJson(topSpots));
        report.setTotalSaved(totalSaved);
        report.setAllowanceReceived(allowanceReceived);
        report.setSavingRate(savingRate);
        // V3에서 조언이 아이용/부모용으로 분리됐다. 개인화된 AI 조언은 아직 없어서
        // 기존과 동일하게 유형 설명을 아이용 조언에 넣고, 부모용은 비워 둔다.
        // TODO: AI 조언이 붙으면 두 값 모두 그쪽에서 채운다.
        report.setChildAdvice(type.getDescription());
        report.setParentAdvice(null);

        childSpendingReportMapper.upsert(report);

        notificationService.create(
                childId,
                NotificationType.SPENDING_REPORT_CREATED,
                String.format("%d년 %d월 소비 리포트가 생성되었습니다.",
                        targetMonth.getYear(),
                        targetMonth.getMonthValue())
        );

        Long parentId = userMapper.selectParentIdByChildId(childId);
        if (parentId != null) {
            notificationService.create(
                    parentId,
                    NotificationType.SPENDING_REPORT_CREATED,
                    String.format("자녀의 %d년 %d월 소비 리포트가 생성되었습니다.",
                            targetMonth.getYear(),
                            targetMonth.getMonthValue())
            );
        }
    }

    private String toJson(List<TopSpotDto> topSpots) {
        try {
            return objectMapper.writeValueAsString(topSpots);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 9개 소비 유형 판정. 위에서부터 순서대로 검사해 처음 맞는 유형으로 확정한다.
     * (SpendingReportClassificationServiceImpl.classify()와 동일 — 다음 단계에서 그쪽은 정리 예정)
     */
    private String classify(
            Long walletId,
            String yearMonth,
            long totalSpent,
            long lastMonthSpent,
            int transactionCount
    ) {
        int achievedPiggyBankCount = spendingClassificationMapper.countAchievedPiggyBanks(walletId, yearMonth);
        if (achievedPiggyBankCount >= SAVING_DREAMER_MIN_ACHIEVED) {
            return "SAVING_DREAMER";
        }

        if (totalSpent == 0) {
            return "ZERO_SPENDING";
        }

        int spendingDayCount = spendingClassificationMapper.findDistinctSpendingDayCount(walletId, yearMonth);
        if (spendingDayCount >= FREQUENT_SPARROW_MIN_DAYS) {
            return "FREQUENT_SPARROW";
        }

        double avgAmount = (double) totalSpent / transactionCount;
        if (avgAmount >= BIG_SPENDER_MIN_AVG_AMOUNT && transactionCount <= BIG_SPENDER_MAX_COUNT) {
            return "BIG_SPENDER";
        }

        double changeRate = lastMonthSpent == 0
                ? Double.MAX_VALUE
                : Math.abs((totalSpent - lastMonthSpent) / (double) lastMonthSpent);
        if (changeRate >= ROLLER_COASTER_RATE) {
            return "ROLLER_COASTER";
        }

        if (transactionCount <= CAREFUL_OWL_MAX_COUNT) {
            return "CAREFUL_OWL";
        }

        long topMerchantAmount = spendingClassificationMapper.findTopMerchantAmount(walletId, yearMonth);
        double merchantShare = (double) topMerchantAmount / totalSpent;
        if (merchantShare >= ONE_STORE_SNIPER_SHARE) {
            return "ONE_STORE_SNIPER";
        }

        if (totalSpent <= SMALL_SAVER_MAX_AMOUNT) {
            return "SMALL_SAVER";
        }

        return "SPROUT";
    }
}