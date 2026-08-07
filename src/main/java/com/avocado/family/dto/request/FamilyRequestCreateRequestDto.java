package com.avocado.family.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@ApiModel(
        value = "FamilyRequestCreateRequest",
        description = "가족 연결 요청 데이터"
)
@Getter
@NoArgsConstructor
public class FamilyRequestCreateRequestDto {

    @NotBlank(message = "초대 코드를 입력해주세요.")
    @Pattern(
            regexp = "^[A-Za-z0-9]{6}$",
            message = "초대 코드는 영문과 숫자 6자리입니다."
    )
    @ApiModelProperty(
            value = "보호자의 초대 코드",
            example = "4B3J2V",
            required = true
    )
    private String code;
}
