package com.avocado.domain.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@ApiModel(description = "아이 선불지갑 조회 응답 데이터")
public class WalletResponseDto {
    @JsonProperty("wallet_id")
    @ApiModelProperty(
            value = "아이 선불 지갑 ID",
            example = "1"
    )
    private Long walletId;

    @JsonProperty("child_id")
    @ApiModelProperty(
            value = "아이 ID",
            example = "15"
    )
    private Long childId;

    @JsonProperty("wallet_number")
    @ApiModelProperty(
            value = "서비스 내부 지갑 식별번호",
            example = "12345678901234"
    )
    private String walletNumber;

    @ApiModelProperty(
            value = "잔액",
            example = "35000"
    )
    private Long balance;

    @ApiModelProperty(
            value = "상태",
            example = "ACTIVE"
    )
    private String status;

    @JsonProperty("created_at")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul"
    )
    @ApiModelProperty(
            value = "생성 일시",
            example = "2026-08-03 17:37:25"
    )
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "Asia/Seoul"
    )
    @ApiModelProperty(
            value = "수정 일시",
            example = "2026-08-03 17:37:25"
    )
    private LocalDateTime updatedAt;
}
