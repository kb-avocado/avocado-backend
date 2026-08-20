package com.avocado.domain.report.controller;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
import com.avocado.domain.report.service.ReportGenerationService;
import com.avocado.domain.report.service.SpendingReportClassificationService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.ApiResponse;
import com.avocado.domain.report.dto.response.ReportResponseDto;
import com.avocado.domain.report.service.ReportService;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.YearMonth;
import java.time.format.DateTimeParseException;

import static com.avocado.global.response.code.SuccessCode.REPORT_FOUND;
import static com.avocado.global.response.code.SuccessCode.SPENDING_REPORT_TYPE_FOUND;

@Api(tags = "리포트 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    // 수동 생성으로 한 번에 만들 수 있는 최대 개월 수
    private static final int MAX_GENERATE_MONTHS = 12;

    private final ReportService reportService;
    private final SpendingReportClassificationService spendingReportClassificationService;
    private final ReportGenerationService reportGenerationService;

    @GetMapping("/{yearMonth}")
    @ApiOperation(
            value = "월별 리포트 조회",
            notes = "소비 유형을 제외한 리포트 데이터를 조회합니다. "
    )
    public ResponseEntity<ApiResponse<ReportResponseDto>> getReport(
            @PathVariable String yearMonth,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        ReportResponseDto data = reportService.getReport(yearMonth, childId, authUser);

        return ResponseEntity
                .status(REPORT_FOUND.getHttpStatus())
                .body(ApiResponse.success(REPORT_FOUND, data));
    }

    @GetMapping("/{yearMonth}/spending-type")
    @ApiOperation(value = "이번 달 소비 유형 조회")
    public ResponseEntity<ApiResponse<SpendingReportTypeDto>> getSpendingType(
            @PathVariable String yearMonth,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        SpendingReportTypeDto data = spendingReportClassificationService.classifyAndSave(yearMonth, childId, authUser);

        return ResponseEntity
                .status(SPENDING_REPORT_TYPE_FOUND.getHttpStatus())
                .body(ApiResponse.success(SPENDING_REPORT_TYPE_FOUND, data));
    }

    /*
     * 리포트 생성 테스트용 임시 엔드포인트. 테스트 후 삭제
     *
     * 리포트는 매월 1일 ReportScheduler가 전달치를 만든다. 그전에 화면을 확인하려면
     * 다음 배치를 기다려야 해서, 같은 계산을 지금 실행할 수 있게 열어둔다.
     * 파라미터를 모두 비우면 배치와 똑같이 "지난달, 전체 자녀"를 만든다.
     */
    @PostMapping("/generate")
    @ApiOperation(
            value = "월별 리포트 수동 생성 (테스트용)",
            notes = "배치를 기다리지 않고 리포트를 즉시 생성한다. 이미 있는 달은 다시 계산한 값으로 덮어쓴다."
    )
    public ResponseEntity<String> generate(
            @ApiParam(value = "생성할 마지막 달 (yyyy-MM). 비우면 지난달", example = "2026-07")
            @RequestParam(required = false) String yearMonth,

            @ApiParam(value = "그 달부터 거슬러 올라가며 만들 개월 수", example = "4")
            @RequestParam(defaultValue = "1") int months,

            @ApiParam(value = "대상 자녀 ID. 비우면 지갑이 ACTIVE인 전체 자녀", example = "19")
            @RequestParam(required = false) Long childId
    ) {
        YearMonth lastMonth = parseYearMonth(yearMonth);

        if (months < 1 || months > MAX_GENERATE_MONTHS) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        // 오래된 달부터 만든다. 화면의 "지난달 대비" 금액이 직전 달 리포트를 참조하기 때문.
        YearMonth firstMonth = lastMonth.minusMonths(months - 1L);
        for (YearMonth target = firstMonth; !target.isAfter(lastMonth); target = target.plusMonths(1)) {
            if (childId == null) {
                reportGenerationService.generateForAllChildren(target);
            } else {
                reportGenerationService.generateForChild(childId, target);
            }
        }

        return ResponseEntity.ok(String.format(
                "%s ~ %s 리포트 생성 완료 (대상: %s)",
                firstMonth,
                lastMonth,
                childId == null ? "전체 자녀" : "자녀 " + childId
        ));
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            return YearMonth.now().minusMonths(1);
        }

        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}