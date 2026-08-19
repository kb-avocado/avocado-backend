package com.avocado.domain.report.controller;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
import com.avocado.domain.report.service.ReportGenerationService;
import com.avocado.domain.report.service.SpendingReportClassificationService;
import com.avocado.global.response.ApiResponse;
import com.avocado.domain.report.dto.response.ReportResponseDto;
import com.avocado.domain.report.service.ReportService;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.avocado.domain.report.service.ReportGenerationService;
import java.time.YearMonth;

import static com.avocado.global.response.code.SuccessCode.REPORT_FOUND;
import static com.avocado.global.response.code.SuccessCode.SPENDING_REPORT_TYPE_FOUND;

@Api(tags = "리포트 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SpendingReportClassificationService spendingReportClassificationService;
    private final ReportGenerationService reportGenerationService;

    @GetMapping("/{yearMonth}")
    @ApiOperation(value = "월별 리포트 조회", notes = "소비 유형·AI 조언을 제외한 리포트 데이터를 조회합니다.")
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

    //리포트 로직 테스트용 API
    @PostMapping("/test/generate/{childId}/{yearMonth}")
    @ApiOperation(
            value = "[TEST] 특정 자녀 월간 리포트 강제 생성",
            notes = "테스트용으로 특정 자녀의 특정 월 리포트를 생성합니다."
    )
    public ResponseEntity<Void> generateReportForTest(
            @PathVariable Long childId,
            @PathVariable String yearMonth
    ) {
        reportGenerationService.generateForAllChildren(
                YearMonth.parse(yearMonth)
        );

        return ResponseEntity.noContent().build();
    }
}