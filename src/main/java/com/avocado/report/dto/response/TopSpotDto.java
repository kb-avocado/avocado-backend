// report/dto/response/TopSpotDto.java
package com.avocado.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TopSpotDto {
    private Integer rank;
    private String category;
    private Long amount;
    private Integer percentage;
}