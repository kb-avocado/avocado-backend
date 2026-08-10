package com.avocado.domain.report.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.report.domain.ChildSpendingReport;
import com.avocado.domain.report.domain.SpendingReportType;
import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
import com.avocado.domain.report.mapper.ChildSpendingReportMapper;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.domain.report.mapper.SpendingClassificationMapper;
import com.avocado.domain.report.mapper.SpendingReportTypeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Transactional
public class SpendingReportClassificationServiceImpl implements SpendingReportClassificationService {

    // TODO: 아래 기준값들은 문서 설명 기준 임의 해석. 기획 확정되면 조정할 것.
    private static final int SAVING_DREAMER_MIN_ACHIEVED = 2;
    private static final int FREQUENT_SPARROW_MIN_DAYS = 25;
    private static final double BIG_SPENDER_MIN_AVG_AMOUNT = 15000;
    private static final int BIG_SPENDER_MAX_COUNT = 5;
    private static final double ROLLER_COASTER_RATE = 0.5;
    private static final int CAREFUL_OWL_MAX_COUNT = 10;
    private static final double ONE_STORE_SNIPER_SHARE = 0.5;
    private static final long SMALL_SAVER_MAX_AMOUNT = 10000;

    private final ReportMapper reportMapper;
    private final SpendingClassificationMapper spendingClassificationMapper;
    private final SpendingReportTypeMapper spendingReportTypeMapper;
    private final ChildSpendingReportMapper childSpendingReportMapper;

    @Override
    public SpendingReportTypeDto classifyAndSave(String yearMonth, Long childId) {
        Long walletId = reportMapper.findWalletIdByChildId(childId);
        if (walletId == null) {
            throw new BusinessException(ErrorCode.WALLET_NOT_FOUND);
        }

        YearMonth targetMonth = YearMonth.parse(yearMonth);
        String previousMonth = targetMonth.minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        long totalSpent = reportMapper.sumSpentAmount(walletId, yearMonth);
        long lastMonthSpent = reportMapper.sumSpentAmount(walletId, previousMonth);
        int transactionCount = reportMapper.countTransactions(walletId, yearMonth);

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
        childSpendingReportMapper.upsert(report);

        return SpendingReportTypeDto.builder()
                .code(type.getCode())
                .name(type.getName())
                .description(type.getDescription())
                .build();
    }

    /**
     * 9개 소비 유형 판정. 위에서부터 순서대로 검사해 처음 맞는 유형으로 확정한다.
     * (spending_report_types에 우선순위 컬럼이 없어서, 이 검사 순서 자체가 우선순위 역할을 함)
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
                ? Double.MAX_VALUE // 지난달 소비 0원 + 이번 달 소비 발생 = 급변으로 취급
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

        long maxPaymentAmount = spendingClassificationMapper.findMaxPaymentAmount(walletId, yearMonth);
        if (maxPaymentAmount <= SMALL_SAVER_MAX_AMOUNT) {
            return "SMALL_SAVER";
        }

        return "SPROUT";
    }
}