package com.avocado.domain.user.dto.response;

import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "회원 가입 응답 데이터")
public class UserSignUpResponseDto {

    @ApiModelProperty(
            value = "회원 ID",
            example = "101",
            required = true
    )
    private final Long userId;

    @ApiModelProperty(
            value = "회원 이름",
            example = "한로로",
            required = true
    )
    private final String name;

    @ApiModelProperty(
            value = "회원 유형",
            example = "PARENT",
            allowableValues = "PARENT, CHILD",
            required = true
    )
    private final UserType type;

    @ApiModelProperty(
            value = "회원 권한",
            example = "USER",
            allowableValues = "USER, ADMIN",
            required = true
    )
    private final UserRole role;

    @ApiModelProperty(
            value = "회원 상태",
            example = "ACTIVE",
            allowableValues = "PENDING, ACTIVE, SUSPENDED, DELETED",
            required = true
    )
    private final UserStatus status;
}
