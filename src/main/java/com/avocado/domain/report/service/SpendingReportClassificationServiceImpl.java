package com.avocado.domain.report.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.report.domain.ChildSpendingReport;
import com.avocado.domain.report.domain.SpendingReportType;
import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
import com.avocado.domain.report.mapper.ChildSpendingReportMapper;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.domain.report.mapper.SpendingClassificationMapper;
import com.avocado.domain.report.mapper.SpendingReportTypeMapper;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional
public class SpendingReportClassificationServiceImpl implements SpendingReportClassificationService {

    private static final int SAVING_DREAMER_MIN_ACHIEVED = 2;
    private static final int FREQUENT_SPARROW_MIN_DAYS = 25;
    private static final double BIG_SPENDER_MIN_AVG_AMOUNT = 15000;
    private static final int BIG_SPENDER_MAX_COUNT = 5;
    private static final double ROLLER_COASTER_RATE = 0.5;
    private static final int CAREFUL_OWL_MAX_COUNT = 10;
    private static final double ONE_STORE_SNIPER_SHARE = 0.5;
    private static final long SMALL_SAVER_MAX_AMOUNT = 10000;

    // 유형별 아이용/부모용 설명. { childDescription, parentDescription } 순서.
    private static final Map<String, String[]> DESCRIPTIONS = Map.ofEntries(
            Map.entry("SAVING_DREAMER", new String[]{
                    "저금 목표를 하나씩 차근차근 채워가고 있어요! 꿈을 향해 열심히 달리는 중이에요.",
                    "이번 달에 저금통을 2개 이상 채웠을 때 나오는 유형이에요."
            }),
            Map.entry("ZERO_SPENDING", new String[]{
                    "이번 달은 지갑도 푹 쉬었어요! 꼭 필요한 순간만 기다리는 멋진 절약가예요.",
                    "이번 달 소비가 0원일 때 나오는 유형이에요."
            }),
            Map.entry("FREQUENT_SPARROW", new String[]{
                    "용돈 쓰는 날이 많았어요! 다음 달엔 하루쯤 쉬어가는 소비도 도전해 보세요.",
                    "한 달 동안 소비한 날이 25일 이상일 때 나오는 유형이에요."
            }),
            Map.entry("BIG_SPENDER", new String[]{
                    "자주 사지는 않지만, 한 번 살 땐 제대로! 신중하게 선택하는 소비 스타일이에요.",
                    "평균 결제 금액이 15,000원 이상이고 소비 횟수가 5회 이하일 때 나오는 유형이에요."
            }),
            Map.entry("ROLLER_COASTER", new String[]{
                    "지난달과 소비 총 금액이 크게 달라졌어요! 이번 달에 무슨 일이 있었나요?",
                    "전월 대비 소비 금액이 50% 이상 증가하거나 감소했을 때 나오는 유형이에요."
            }),
            Map.entry("CAREFUL_OWL", new String[]{
                    "꼭 필요한 순간에만 소비하는 신중한 스타일이에요! 한 번 더 생각하는 습관이 멋져요.",
                    "이번 달 소비 횟수가 10회 이하일 때 나오는 유형이에요."
            }),
            Map.entry("ONE_STORE_SNIPER", new String[]{
                    "좋아하는 곳은 한 번 빠지면 계속 가게 돼요! 단골 가게가 생겼네요.",
                    "한 가게에서 사용한 금액이 전체 소비의 50% 이상일 때 나오는 유형이에요."
            }),
            Map.entry("SMALL_SAVER", new String[]{
                    "작은 돈도 소중하게 아끼는 습관이 있어요! 차곡차곡 모이면 큰 힘이 된답니다.",
                    "이번 달 총 소비 금액이 10,000원 이하일 때 나오는 유형이에요."
            }),
            Map.entry("SPROUT", new String[]{
                    "차근차근 나만의 소비 습관을 만들어가는 중이에요! 다음 달에는 어떤 유형이 될지 기대돼요.",
                    "특정 소비 패턴에 해당하지 않을 때 나오는 기본 유형이에요."
            })
    );

    private final ReportMapper reportMapper;
    private final SpendingClassificationMapper spendingClassificationMapper;
    private final SpendingReportTypeMapper spendingReportTypeMapper;
    private final ChildSpendingReportMapper childSpendingReportMapper;
    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;

    @Override
    public SpendingReportTypeDto classifyAndSave(String yearMonth, Long childId, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        YearMonth targetMonth = YearMonth.parse(yearMonth);

        // 이미 이 달에 계산된 기록이 있으면 재계산하지 않고 그대로 반환 (한 달에 한 번만 계산)
        ChildSpendingReport existing = childSpendingReportMapper.findByChildAndYearMonth(
                targetChildId, targetMonth.getYear(), targetMonth.getMonthValue());

        if (existing != null) {
            SpendingReportType cachedType = spendingReportTypeMapper.findById(existing.getReportTypeId());
            if (cachedType == null) {
                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
            }
            return toDto(cachedType, targetMonth);
        }

        return calculateAndSave(yearMonth, targetChildId, targetMonth);
    }

    /**
     * 그 달 최초 조회 시에만 실행되는 실제 판정 로직.
     */
    private SpendingReportTypeDto calculateAndSave(String yearMonth, Long childId, YearMonth targetMonth) {
        Long walletId = reportMapper.findWalletIdByChildId(childId);
        if (walletId == null) {
            throw new BusinessException(ErrorCode.WALLET_NOT_FOUND);
        }

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

        return toDto(type, targetMonth);
    }

    private SpendingReportTypeDto toDto(SpendingReportType type, YearMonth targetMonth) {
        String[] descriptions = DESCRIPTIONS.getOrDefault(
                type.getCode(), new String[]{type.getDescription(), type.getDescription()});

        return SpendingReportTypeDto.builder()
                .code(type.getCode())
                .name(type.getName())
                .childDescription(descriptions[0])
                .parentDescription(descriptions[1])
                .percentage(calculatePercentage(type.getId(), targetMonth))
                .build();
    }

    // 같은 달, 같은 유형으로 분류된 아이 비율(%). 아직 아무도 집계 안 됐으면(분모 0) null.
    private Integer calculatePercentage(Long reportTypeId, YearMonth targetMonth) {
        long total = childSpendingReportMapper.countAllByYearMonth(
                targetMonth.getYear(), targetMonth.getMonthValue());
        if (total == 0) {
            return null;
        }

        long sameType = childSpendingReportMapper.countByReportTypeAndYearMonth(
                reportTypeId, targetMonth.getYear(), targetMonth.getMonthValue());

        return (int) Math.round(sameType * 100.0 / total);
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

        if (totalSpent <= SMALL_SAVER_MAX_AMOUNT) {
            return "SMALL_SAVER";
        }

        return "SPROUT";
    }

    /*
     * 요청받은 childId와 로그인 사용자 정보를 바탕으로
     * 실제 조회 대상 childId를 결정하고 접근 권한을 검증한다.
     * News/Home/ReportServiceImpl과 동일한 패턴이다.
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