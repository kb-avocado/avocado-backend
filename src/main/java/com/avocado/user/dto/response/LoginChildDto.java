package com.avocado.user.dto.response;

import com.avocado.user.domain.UserStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 부모 로그인 시 내려주는 연결된 아이 정보. MyBatis가 직접 매핑하므로 기본 생성자가 필요하다.
@ApiModel(description = "연결된 아이 정보")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginChildDto {

    @ApiModelProperty(
            value = "아이 회원 ID",
            example = "10",
            required = true
    )
    private Long id;

    @ApiModelProperty(
            value = "아이 이름",
            example = "고길동",
            required = true
    )
    private String name;

    @ApiModelProperty(
            value = "아이 계정 상태",
            example = "ACTIVE",
            allowableValues = "PENDING, ACTIVE, SUSPENDED, DELETED",
            required = true
    )
    private UserStatus status;

    @ApiModelProperty(
            value = "아이 선불지갑 ID (지갑이 없으면 응답에서 제외)",
            example = "10"
    )
    private Long walletId;
}
