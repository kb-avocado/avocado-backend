package com.avocado.domain.user.dto.response;

import com.avocado.domain.user.domain.UserType;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 마이페이지에 보여줄 내 정보.
 * - PARENT: inviteCode
 * - CHILD : parentName, parentEmail
 */
@ApiModel(description = "마이페이지 내 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MyPageResponseDto {

    @ApiModelProperty(
            value = "회원 ID",
            example = "101",
            required = true
    )
    private Long userId;

    @ApiModelProperty(
            value = "회원 이름",
            example = "홍길동",
            required = true
    )
    private String name;

    @ApiModelProperty(
            value = "회원 이메일",
            example = "hong@avocado.com",
            required = true
    )
    private String email;

    @ApiModelProperty(
            value = "회원 유형. 이 값으로 보여줄 화면을 고른다.",
            example = "PARENT",
            allowableValues = "PARENT, CHILD",
            required = true
    )
    private UserType type;

    @ApiModelProperty(
            value = "[PARENT] 가족 초대 코드 (CHILD면 응답에서 제외)",
            example = "A1B2C3"
    )
    private String inviteCode;

    @ApiModelProperty(
            value = "[CHILD] 연결된 보호자 이름 (아직 연결 전이거나 PARENT면 응답에서 제외)",
            example = "홍길동"
    )
    private String parentName;

    @ApiModelProperty(
            value = "[CHILD] 연결된 보호자 이메일 (아직 연결 전이거나 PARENT면 응답에서 제외)",
            example = "hong@avocado.com"
    )
    private String parentEmail;
}
