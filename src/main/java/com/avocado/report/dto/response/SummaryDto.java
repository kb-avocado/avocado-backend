// report/dto/response/SummaryDto.java
package com.avocado.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SummaryDto {
    private Long totalSpent;
    private Long comparedToLastMonth; // 음수면 절약, 양수면 더 씀
    private Integer transactionCount;
}