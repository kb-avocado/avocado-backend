package com.avocado.domain.family.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@ApiModel(
        value = "FamilyRequestConfirmRequest",
        description = "아이의 가족 연결 최종 확정 데이터"
)
@Getter
@NoArgsConstructor
public class FamilyRequestConfirmRequestDto {

    // 원시 타입 boolean이면 값이 없을 때 false로 채워져 거절로 처리된다. 래퍼 타입이어야 누락을 잡을 수 있다.
    @NotNull(message = "연결 여부를 선택해주세요.")
    @ApiModelProperty(
            value = "보호자를 확인하고 연결할지 여부",
            example = "true",
            required = true
    )
    private Boolean confirm;
}
