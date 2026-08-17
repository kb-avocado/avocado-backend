package com.avocado.domain.payment.dto.response;

import com.avocado.domain.payment.domain.PaymentQrActiveTokenVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@ApiModel(description = "결제 대기 QR 토큰 응답")
@Getter
@Builder
public class PaymentQrActiveTokenResponseDto {

    @ApiModelProperty(
            value = "결제용 QR 토큰",
            example = "example-qr-token",
            required = true
    )
    private final String token;

    @ApiModelProperty(
            value = "토큰을 발급한 사용자 ID",
            example = "102",
            required = true
    )
    private final Long userId;

    @ApiModelProperty(
            value = "토큰을 발급한 사용자 이름. 사용자 정보가 없으면 null입니다.",
            example = "김아보"
    )
    private final String userName;

    @ApiModelProperty(
            value = "토큰 만료까지 남은 시간(초)",
            example = "120",
            required = true
    )
    private final long expiresIn;

    public static PaymentQrActiveTokenResponseDto from(PaymentQrActiveTokenVo activeToken) {
        return PaymentQrActiveTokenResponseDto.builder()
                .token(activeToken.getToken())
                .userId(activeToken.getUserId())
                .expiresIn(activeToken.getExpiresIn())
                .build();
    }

    public static PaymentQrActiveTokenResponseDto from(
            PaymentQrActiveTokenVo activeToken,
            String userName
    ) {
        return PaymentQrActiveTokenResponseDto.builder()
                .token(activeToken.getToken())
                .userId(activeToken.getUserId())
                .userName(userName)
                .expiresIn(activeToken.getExpiresIn())
                .build();
    }
}
