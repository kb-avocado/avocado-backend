// report/dto/response/SavingsDto.java
package com.avocado.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SavingsDto {
    private Long totalSaved;
    private Integer savingsRate; // (nullable) 용돈 지급 테이블 없어서 지금은 null 처리
}