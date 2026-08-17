package com.avocado.domain.report.controller;

import com.avocado.domain.report.dto.response.SpendingReportTypeDto;
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

import static com.avocado.global.response.code.SuccessCode.REPORT_FOUND;
import static com.avocado.global.response.code.SuccessCode.SPENDING_REPORT_TYPE_FOUND;

@Api(tags = "리포트 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final SpendingReportClassificationService spendingReportClassificationService;

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
}