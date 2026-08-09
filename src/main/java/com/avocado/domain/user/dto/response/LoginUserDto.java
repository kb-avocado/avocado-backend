package com.avocado.domain.user.dto.response;

import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * 로그인한 회원 정보.
 * 회원 타입에 따라 내려가는 필드가 달라서, 값이 없는 필드는 응답에서 아예 제외한다.
 * - PARENT: accountId, child
 * - CHILD : walletId, parentId (PENDING이면 parentId 대신 family)
 */
@ApiModel(description = "로그인한 회원 정보")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginUserDto {

    @ApiModelProperty(
            value = "회원 ID",
            example = "101",
            required = true
    )
    private final Long userId;

    @ApiModelProperty(
            value = "회원 이름",
            example = "홍길동",
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

    @ApiModelProperty(
            value = "[PARENT] 연동 계좌 ID (연동된 계좌가 없으면 제외)",
            example = "123"
    )
    private final Long accountId;

    @ApiModelProperty(value = "[PARENT] 연결된 아이 목록")
    private final List<LoginChildDto> child;

    @ApiModelProperty(
            value = "[CHILD] 선불지갑 ID",
            example = "10"
    )
    private final Long walletId;

    @ApiModelProperty(
            value = "[CHILD] 연결된 부모 회원 ID",
            example = "1"
    )
    private final Long parentId;

    @ApiModelProperty(value = "[CHILD] 가족 연결 대기 정보 (PENDING일 때만)")
    private final LoginFamilyDto family;
}
