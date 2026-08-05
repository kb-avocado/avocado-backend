// report/domain/MonthlySpentRow.java
package com.avocado.report.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MonthlySpentRow {
    private String yearMonth;
    private Long amount;
}