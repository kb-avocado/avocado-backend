package com.avocado.domain.payment.dto.response;

import com.avocado.domain.payment.domain.PaymentQrStatusVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@ApiModel(description = "결제 QR 상태 조회 응답")
@Getter
@Builder
public class PaymentQrStatusResponseDto {

    @ApiModelProperty(
            value = "QR 결제 상태",
            allowableValues = "WAITING, SUCCESS, FAILED, EXPIRED, INVALID",
            required = true
    )
    private final String status;

    @ApiModelProperty(value = "토큰 만료까지 남은 시간(초)")
    private final Long expiresIn;

    @ApiModelProperty(value = "선불지갑 거래 이력 ID")
    private final Long walletHistoryId;

    @ApiModelProperty(value = "가맹점 ID")
    private final Long merchantId;

    @ApiModelProperty(value = "가맹점 이름")
    private final String merchantName;

    @ApiModelProperty(value = "결제 금액")
    private final Long amount;

    @ApiModelProperty(value = "실패 코드")
    private final String failureCode;

    @ApiModelProperty(value = "거래 후 선불지갑 잔액")
    private final Long balanceAfter;

    public static PaymentQrStatusResponseDto from(PaymentQrStatusVo status) {
        return PaymentQrStatusResponseDto.builder()
                .status(status.getStatus().name())
                .expiresIn(status.getExpiresIn())
                .walletHistoryId(status.getWalletHistoryId())
                .merchantId(status.getMerchantId())
                .merchantName(status.getMerchantName())
                .amount(status.getAmount())
                .failureCode(status.getFailureCode())
                .balanceAfter(status.getBalanceAfter())
                .build();
    }
}
