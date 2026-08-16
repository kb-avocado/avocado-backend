package com.avocado.domain.payment.dto.response;

import com.avocado.domain.payment.domain.PaymentFailureCode;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@ApiModel(description = "POS 결제 시뮬레이션 응답")
@Getter
@Builder
public class PaymentSimulationResponseDto {

    @ApiModelProperty(value = "선불지갑 거래 이력 ID")
    private final Long walletHistoryId;

    @ApiModelProperty(value = "가맹점 ID")
    private final Long merchantId;

    @ApiModelProperty(value = "가맹점 이름")
    private final String merchantName;

    @ApiModelProperty(value = "결제 금액")
    private final Long amount;

    @ApiModelProperty(value = "결제 처리 상태", allowableValues = "SUCCESS, FAILED")
    private final String status;

    @ApiModelProperty(value = "실패 코드")
    private final PaymentFailureCode failureCode;

    @ApiModelProperty(value = "거래 후 선불지갑 잔액")
    private final Long balanceAfter;
}
