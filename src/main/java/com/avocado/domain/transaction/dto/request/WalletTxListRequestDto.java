package com.avocado.domain.transaction.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "선불지갑 거래 기본 내역 조회 요청 파라미터")
public class WalletTxListRequestDto {

    @ApiModelProperty(
            value = "페이지 번호",
            example = "0"
    )
    private int page = 0;

    @ApiModelProperty(
            value = "페이지당 데이터 수",
            example = "20"
    )
    private int size = 20;
}
