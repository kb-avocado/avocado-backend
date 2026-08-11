package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_TOKEN_ISSUED;
import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_TOKEN_REISSUED;

@Api(tags = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation(
            value = "결제 QR 토큰 발급",
            notes = "로그인한 사용자 기준으로 결제 QR 토큰을 발급합니다."
    )
    @PostMapping("/qr")
    public ResponseEntity<ApiResponse<PaymentQrTokenResponseDto>> issuePaymentQrToken(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentQrTokenResponseDto response = paymentService.issuePaymentQrToken(authUser);

        return ResponseEntity
                .status(PAYMENT_QR_TOKEN_ISSUED.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_TOKEN_ISSUED, response));
    }

    @ApiOperation(
            value = "결제 QR 토큰 재발급",
            notes = "기존 결제 QR 토큰을 무효화하고 새 결제 QR 토큰을 발급합니다."
    )
    @PostMapping("/qr/reissue")
    public ResponseEntity<ApiResponse<PaymentQrTokenResponseDto>> reissuePaymentQrToken(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentQrTokenResponseDto response = paymentService.reissuePaymentQrToken(authUser);

        return ResponseEntity
                .status(PAYMENT_QR_TOKEN_REISSUED.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_TOKEN_REISSUED, response));
    }
}
