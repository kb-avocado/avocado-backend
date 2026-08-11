package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
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
            notes = "POST /api/payments/qr/reissue\n"
                    + "\n"
                    + "Headers\n"
                    + "- Cookie: accessToken={JWT}\n"
                    + "- Content-Type: application/json\n"
                    + "\n"
                    + "Request Body\n"
                    + "- 없음\n"
                    + "\n"
                    + "동작\n"
                    + "- 로그인 사용자의 기존 결제 QR 토큰이 있으면 즉시 무효화하고, "
                    + "새 QR 토큰을 TTL과 함께 Redis에 저장합니다.\n"
                    + "\n"
                    + "Response Body\n"
                    + "- success: true\n"
                    + "- code: PAY-002\n"
                    + "- message: 결제 QR 토큰이 재발급되었습니다.\n"
                    + "- data.token: 새 결제 QR 토큰\n"
                    + "- data.expiresIn: 만료까지 남은 시간(초)"
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "Cookie",
                    value = "HttpOnly Access Token 쿠키. 예: accessToken={JWT}",
                    required = true,
                    dataType = "string",
                    paramType = "header"
            )
    })
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "PAY-002: 결제 QR 토큰이 재발급되었습니다."
            ),
            @io.swagger.annotations.ApiResponse(
                    code = 401,
                    message = "AUT-001: 인증이 필요합니다."
            )
    })
    @PostMapping(
            value = "/qr/reissue",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<PaymentQrTokenResponseDto>> reissuePaymentQrToken(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        PaymentQrTokenResponseDto response = paymentService.reissuePaymentQrToken(authUser);

        return ResponseEntity
                .status(PAYMENT_QR_TOKEN_REISSUED.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_TOKEN_REISSUED, response));
    }
}
