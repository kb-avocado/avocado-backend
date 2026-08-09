package com.avocado.domain.wallet.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "아이 선불 지갑 결제용 QR 코드 발급 응답 데이터")
public class WalletQrResponseDto {

    @ApiModelProperty(value = "바코드 ID", example = "1")
    private Long id;

    @JsonProperty("wallet_id")
    @ApiModelProperty(value = "지갑 ID", example = "10")
    private Long walletId;

    @ApiModelProperty(value = "결제용 토큰", example = "BRC_8f24dbfa7c934f20a3bc91e7")
    private String token;

    @ApiModelProperty(value = "바코드 상태", example = "ACTIVE")
    private String status;

    @JsonProperty("created_at")
    @ApiModelProperty(value = "생성 일시", example = "2026-07-28T12:32:00")
    private String createdAt;

    @JsonProperty("expired_at")
    @ApiModelProperty(value = "만료 일시", example = "2026-07-28T12:35:00")
    private String expiredAt;
}
