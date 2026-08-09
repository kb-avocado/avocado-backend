// report/dto/response/NavigationDto.java
package com.avocado.domain.report.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NavigationDto {
    private boolean hasPrevious;
    private boolean hasNext;
}