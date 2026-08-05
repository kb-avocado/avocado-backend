package com.avocado.user.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@ApiModel(description = "로그인 응답 데이터")
@Getter
@Builder
public class UserLoginResponseDto {

    @ApiModelProperty(
            value = "로그인한 회원 정보",
            required = true
    )
    private final LoginUserDto user;
}
