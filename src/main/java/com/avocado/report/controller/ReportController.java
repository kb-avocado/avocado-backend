package com.avocado.report.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.common.response.code.SuccessCode;
import com.avocado.report.dto.response.ReportResponseDto;
import com.avocado.report.service.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.avocado.common.response.code.SuccessCode.*;

// TODO: 로그인 붙으면 childId를 토큰에서 꺼내도록 교체. 지금은 데모용 임시 고정값 사용
@Api(tags = "리포트 API")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    private static final Long TEMP_CHILD_ID = 12L; // TODO: 로그인 붙으면 제거

    @GetMapping("/{yearMonth}")
    @ApiOperation(value = "월별 리포트 조회", notes = "소비 유형·AI 조언을 제외한 리포트 데이터를 조회합니다.")
    public ResponseEntity<ApiResponse<ReportResponseDto>> getReport(
            @PathVariable String yearMonth,
            @RequestParam(required = false) Long childId
    ) {
        Long targetChildId = childId != null ? childId : TEMP_CHILD_ID;
        ReportResponseDto data = reportService.getReport(yearMonth, targetChildId);

        return ResponseEntity
                .status(REPORT_FOUND.getHttpStatus())
                .body(ApiResponse.success(REPORT_FOUND, data));
    }
}