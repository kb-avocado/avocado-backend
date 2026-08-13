package com.avocado.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SpendingReportTypeDto {
    private String code;
    private String name;
    private String childDescription;
    private String parentDescription;
    private Integer percentage;
}
