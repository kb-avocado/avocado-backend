package com.avocado.domain.payment.dto.response;

import com.avocado.domain.payment.domain.PaymentQrTokenVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@ApiModel(description = "결제용 QR 토큰 응답")
@Getter
@Builder
public class PaymentQrTokenResponseDto {

    @ApiModelProperty(
            value = "결제용 QR 토큰",
            required = true
    )
    private final String token;

    @ApiModelProperty(
            value = "토큰 만료까지 남은 시간(초)",
            example = "180",
            required = true
    )
    private final long expiresIn;

    public static PaymentQrTokenResponseDto from(PaymentQrTokenVo paymentQrToken) {
        return PaymentQrTokenResponseDto.builder()
                .token(paymentQrToken.getToken())
                .expiresIn(paymentQrToken.getExpiresIn())
                .build();
    }
}
