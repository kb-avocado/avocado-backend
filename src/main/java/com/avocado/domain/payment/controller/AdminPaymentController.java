package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.PAYMENT_SIMULATION_PROCESSED;

@Api(tags = "관리자 결제 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/payments")
public class AdminPaymentController {

    private final PaymentService paymentService;

    @ApiOperation(
            value = "POS 결제 시뮬레이션",
            notes = "POS 시뮬레이터가 QR 토큰으로 결제 성공 또는 임의 실패를 요청합니다."
    )
    @PostMapping(
            value = "/simulate",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<PaymentSimulationResponseDto>> simulatePayment(
            @Valid @RequestBody PaymentSimulationRequestDto request
    ) {
        PaymentSimulationResponseDto response = paymentService.simulatePayment(request);

        return ResponseEntity
                .status(PAYMENT_SIMULATION_PROCESSED.getHttpStatus())
                .body(ApiResponse.success(PAYMENT_SIMULATION_PROCESSED, response));
    }
}
