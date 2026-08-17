package com.avocado.domain.report.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChildSpendingReport {
    private Long id;
    private Long childId;
    private Long reportTypeId;
    private Integer reportYear;
    private Integer reportMonth;
}
