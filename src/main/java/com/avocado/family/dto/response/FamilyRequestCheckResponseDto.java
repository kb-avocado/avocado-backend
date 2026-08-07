package com.avocado.family.dto.response;

import com.avocado.family.domain.FamilyRelationStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 보호자가 확인하는 가족 연결 요청 정보.
 * 보호자는 "누가 언제 요청했는지"를 보고 승인할지 정하므로, 아이 쪽 응답과 필요한 값이 다르다.
 */
@ApiModel(description = "가족 연결 요청 정보 (보호자용)")
@Getter
@Builder
public class FamilyRequestCheckResponseDto {

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
            value = "요청을 보낸 아이 이름",
            example = "김지원",
            required = true
    )
    private final String childName;

    @ApiModelProperty(
            value = "요청 시각",
            example = "2026-08-07T13:26:28",
            required = true
    )
    private final LocalDateTime createdAt;
}
