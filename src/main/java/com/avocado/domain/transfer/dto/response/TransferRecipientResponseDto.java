package com.avocado.domain.transfer.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "송금 대상 조회 응답 데이터")
public class TransferRecipientResponseDto {

    @ApiModelProperty(
            value = "송금 대상 유형",
            example = "WALLET"
    )
    private String recipientType;

    @ApiModelProperty(
            value = "송금 대상 ID",
            example = "15"
    )
    private Long recipientId;

    @ApiModelProperty(
            value = "송금 대상 이름",
            example = "김효정"
    )
    private String recipientName;

    @ApiModelProperty(
            value = "마스킹된 지갑 계좌번호",
            example = "1000-****-5678"
    )
    private String accountNumber;

    @ApiModelProperty(
            value = "사용자 코드",
            example = "AVO1234"
    )
    private String userCode;
}
