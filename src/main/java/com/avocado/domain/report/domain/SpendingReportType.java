package com.avocado.domain.report.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpendingReportType {
    private Long id;
    private String code;
    private String name;
    private String description;
}