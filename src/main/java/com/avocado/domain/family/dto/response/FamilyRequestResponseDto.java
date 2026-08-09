package com.avocado.domain.family.dto.response;

import com.avocado.domain.family.domain.FamilyRelationStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 아이가 보는 가족 연결 요청 정보.
 * 요청을 만든 직후(POST)와 대기 화면의 폴링(GET)에서 같은 모양으로 내려간다.
 * 아이는 "누구에게 보냈고 어디까지 진행됐는지"만 알면 된다.
 */
@ApiModel(description = "가족 연결 요청 정보 (아이용)")
@Getter
@Builder
public class FamilyRequestResponseDto {

    @ApiModelProperty(
            value = "가족 연결 요청 ID",
            example = "1004",
            required = true
    )
    private final Long requestId;

    @ApiModelProperty(
            value = "요청 상태",
            example = "PENDING",
            allowableValues = "PENDING, APPROVED, REJECTED, CANCELED, ACTIVE",
            required = true
    )
    private final FamilyRelationStatus status;

    @ApiModelProperty(
            value = "요청을 받은 보호자 이름",
            example = "김민준",
            required = true
    )
    private final String parentName;
}
