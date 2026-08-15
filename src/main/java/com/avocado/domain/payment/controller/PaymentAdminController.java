package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_ACTIVE_TOKENS_FOUND;

@Api(tags = "관리자 결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class PaymentAdminController {

    private final PaymentService paymentService;

    @ApiOperation(
            value = "결제 대기 QR 토큰 목록 조회",
            notes = "GET /api/admin/payments/qr-tokens\n"
                    + "\n"
                    + "POS 시뮬레이터가 QR을 직접 스캔하지 않고도 현재 결제 대기 중인 활성 QR 토큰 목록을 조회합니다.\n"
                    + "조회 전 Redis의 만료 토큰을 정리하고, 유효한 토큰만 token, userId, expiresIn으로 반환합니다.",
            response = PaymentQrActiveTokenResponseDto.class,
            responseContainer = "List"
    )
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "PAY-004: 결제 대기 중인 QR 토큰 목록을 조회했습니다.",
                    response = PaymentQrActiveTokenResponseDto.class,
                    responseContainer = "List"
            )
    })
    @GetMapping(
            value = "/qr-tokens",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<List<PaymentQrActiveTokenResponseDto>>> getActivePaymentQrTokens() {
        List<PaymentQrActiveTokenResponseDto> response = paymentService.getActivePaymentQrTokens();

        return ResponseEntity
                .status(PAYMENT_QR_ACTIVE_TOKENS_FOUND.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_ACTIVE_TOKENS_FOUND, response));
    }
}
