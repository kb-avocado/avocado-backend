package com.avocado.transaction.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@ApiModel(description = "지갑 거래 내역 리스트 단일 항목")
public class WalletTransactionListItemDto {

    @JsonProperty("history_id")
    @ApiModelProperty(
            value = "지갑 거래 이력 고유 ID",
            example = "105"
    )
    private Long historyId;

    @ApiModelProperty(
            value = "거래 대상명",
            example = "CU 판교점"
    )
    private String title;

    @JsonProperty("ledger_type")
    @ApiModelProperty(
            value = "입출금 여부",
            example = "OUT"
    )
    private String ledgerType;

    @ApiModelProperty(
            value = "거래 금액",
            example = "5000"
    )
    private Long amount;

    @JsonProperty("balance_before")
    @ApiModelProperty(
            value = "거래 전 잔액",
            example = "15000"
    )
    private Long balanceBefore;

    @JsonProperty("balance_after")
    @ApiModelProperty(
            value = "거래 후 잔액",
            example = "10000"
    )
    private Long balanceAfter;

    @JsonProperty("created_at")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    @ApiModelProperty(
            value = "거래 일시",
            example = "2026-08-04 15:30:00"
    )
    private LocalDateTime createAt;
}
