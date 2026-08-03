package com.avocado.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@ApiModel(description = "아이 선불지갑 조회 응답 데이터")
public class WalletResponse {

    @ApiModelProperty(value = "아이 선불 지갑 ID", example = "1")
    private Long id;

    @JsonProperty("child_id")
    @ApiModelProperty(value = "아이 ID", example = "10")
    private Long childId;

    @ApiModelProperty(value = "잔액", example = "35000")
    private Integer balance;

    @ApiModelProperty(value = "상태", example = "ACTIVE")
    private String status;

    @JsonProperty("created_at")
    @ApiModelProperty(value = "생성 일시", example = "2026-07-29 10:49:06.123456")
    private String createdAt;

    @JsonProperty("updated_at")
    @ApiModelProperty(value = "수정 일시", example = "2026-07-29 10:49:06.123456")
    private String updatedAt;
}
