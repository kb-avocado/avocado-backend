package com.avocado.domain.family.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@ApiModel(
        value = "FamilyRequestDecisionRequest",
        description = "보호자의 가족 연결 요청 처리 데이터"
)
@Getter
@NoArgsConstructor
public class FamilyRequestDecisionRequestDto {

    @NotNull(message = "승인 또는 거절을 선택해주세요.")
    @ApiModelProperty(
            value = "보호자의 결정",
            example = "APPROVE",
            allowableValues = "APPROVE, REJECT",
            required = true
    )
    private FamilyRequestDecision decision;
}
