package com.avocado.domain.payment.dto.request;

import com.avocado.domain.payment.domain.PaymentRequestedResult;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@ApiModel(description = "POS 결제 시뮬레이션 요청")
@Getter
@Setter
public class PaymentSimulationRequestDto {

    @ApiModelProperty(
            value = "결제 QR 토큰",
            required = true
    )
    @NotBlank
    private String qrToken;

    @ApiModelProperty(
            value = "가맹점 ID",
            example = "3003",
            required = true
    )
    @NotNull
    private Long merchantId;

    @ApiModelProperty(
            value = "결제 금액",
            example = "12000",
            required = true
    )
    @NotNull
    @Positive
    private Long amount;

    @ApiModelProperty(
            value = "POS 시뮬레이터 요청 결과",
            allowableValues = "SUCCESS, FORCE_FAIL",
            required = true
    )
    @NotNull
    private PaymentRequestedResult requestedResult;
}
