// report/dto/response/TopSpotDto.java
package com.avocado.domain.report.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopSpotDto {
    private Integer rank;
    private String category;
    private Long amount;
    private Integer percentage;
}