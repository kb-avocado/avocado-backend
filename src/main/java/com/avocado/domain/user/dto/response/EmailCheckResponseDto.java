package com.avocado.domain.user.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "이메일 중복 확인 응답 데이터")
public class EmailCheckResponseDto {

    @ApiModelProperty(
            value = "실제로 확인한 이메일",
            example = "hanroro@avocado.kr",
            notes = "가입 때와 동일하게, 앞뒤 공백을 없애고 소문자로 바꾼 값이다.",
            required = true
    )
    private final String email;

    @ApiModelProperty(
            value = "사용 가능 여부",
            example = "true",
            notes = "false면 이미 가입된 이메일이다.",
            required = true
    )
    private final boolean available;
}
