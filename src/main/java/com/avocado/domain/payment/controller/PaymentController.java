package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrStatusResponseDto;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_ACTIVE_TOKENS_FOUND;
import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_STATUS_FOUND;
import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_TOKEN_ISSUED;
import static com.avocado.global.response.code.SuccessCode.PAYMENT_QR_TOKEN_REISSUED;

@Api(tags = "결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @ApiOperation(
            value = "결제 대기 QR 토큰 목록 조회",
            notes = "POS 시뮬레이터가 현재 결제 대기 중인 QR 토큰 목록을 조회합니다."
    )
    @GetMapping(
            value = "/qr/active-tokens",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<List<PaymentQrActiveTokenResponseDto>>> getActivePaymentQrTokens() {
        List<PaymentQrActiveTokenResponseDto> response = paymentService.getActivePaymentQrTokens();

        return ResponseEntity
                .status(PAYMENT_QR_ACTIVE_TOKENS_FOUND.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_ACTIVE_TOKENS_FOUND, response));
    }

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
                    + "- 같은 사용자의 너무 잦은 재발급 요청은 429로 거절합니다.\n"
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
            ),
            @io.swagger.annotations.ApiResponse(
                    code = 429,
                    message = "PAY-003: QR 토큰 재발급 요청이 너무 잦습니다. 잠시 후 다시 시도해주세요."
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

    @ApiOperation(
            value = "결제 QR 상태 조회",
            notes = "GET /api/payments/qr/status?token={qrToken}\n"
                    + "\n"
                    + "QR 대기 화면이 1~2초 간격으로 polling하여 현재 결제 상태를 조회합니다.\n"
                    + "WAITING이면 대기 화면을 유지하고, SUCCESS 또는 FAILED이면 결제 결과 화면으로 전환합니다.\n"
                    + "EXPIRED는 만료된 QR, INVALID는 로그인 사용자와 일치하지 않거나 알 수 없는 QR입니다.",
            response = PaymentQrStatusResponseDto.class
    )
    @ApiImplicitParams({
            @ApiImplicitParam(
                    name = "Cookie",
                    value = "HttpOnly Access Token 쿠키. 예: accessToken={JWT}",
                    required = true,
                    dataType = "string",
                    paramType = "header"
            ),
            @ApiImplicitParam(
                    name = "token",
                    value = "상태를 조회할 결제 QR 토큰",
                    required = true,
                    dataType = "string",
                    paramType = "query"
            )
    })
    @ApiResponses({
            @io.swagger.annotations.ApiResponse(
                    code = 200,
                    message = "PAY-006: 결제 QR 상태를 조회했습니다.",
                    response = PaymentQrStatusResponseDto.class
            ),
            @io.swagger.annotations.ApiResponse(
                    code = 400,
                    message = "COM-001: 요청 값이 올바르지 않습니다."
            ),
            @io.swagger.annotations.ApiResponse(
                    code = 401,
                    message = "AUT-001: 인증이 필요합니다."
            )
    })
    @GetMapping(
            value = "/qr/status",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<PaymentQrStatusResponseDto>> getPaymentQrStatus(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestParam("token") String qrToken
    ) {
        PaymentQrStatusResponseDto response = paymentService.getPaymentQrStatus(
                authUser,
                qrToken
        );

        return ResponseEntity
                .status(PAYMENT_QR_STATUS_FOUND.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_QR_STATUS_FOUND, response));
    }
}
