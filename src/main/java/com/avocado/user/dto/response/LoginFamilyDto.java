package com.avocado.user.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 아이가 PENDING일 때 내려주는 가족 연결 요청 정보. MyBatis가 직접 매핑한다.
@ApiModel(description = "가족 연결 상태")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginFamilyDto {

    // family_relations.status. 해당 enum이 아직 자바에 없어 문자열로 전달한다.
    @ApiModelProperty(
            value = "가족 연결 상태",
            example = "PENDING",
            allowableValues = "PENDING, ACTIVE, REJECTED, CONFIRMED, CANCELED",
            required = true
    )
    private String status;

    @ApiModelProperty(
            value = "가족 연결 요청 ID (family_relations.relation_id)",
            example = "123",
            required = true
    )
    private Long requestId;
}
