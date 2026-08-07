package com.avocado.family.dto.response;

import com.avocado.family.domain.FamilyRelationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 가족 연결 요청 한 건의 상태.
 * 아이 화면과 보호자 화면이 보는 정보가 달라서, 채워지지 않은 필드는 응답에서 제외한다.
 * - 아이: status, parentName (보호자가 누구인지, 어디까지 진행됐는지)
 * - 보호자: childName, createdAt (누가 언제 요청했는지)
 */
@ApiModel(description = "가족 연결 요청 정보")
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
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
            value = "[아이 화면] 보호자 이름",
            example = "김민준"
    )
    private final String parentName;

    @ApiModelProperty(
            value = "[보호자 화면] 요청한 아이 이름",
            example = "김지원"
    )
    private final String childName;

    @ApiModelProperty(
            value = "[보호자 화면] 요청 시각",
            example = "2026-08-07T13:26:28"
    )
    private final LocalDateTime createdAt;
}
