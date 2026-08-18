package com.avocado.domain.report.domain;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ChildSpendingReport {
    private Long id;
    private Long childId;
    private Long reportTypeId;
    private Integer reportYear;
    private Integer reportMonth;
    private Long totalSpent;
    private Integer transactionCount;
    private String topSpots;          // TOP 5 가맹점 목록을 JSON 문자열로 저장
    private Long totalSaved;
    private Long allowanceReceived;
    private BigDecimal savingRate;
    private String advice;
}