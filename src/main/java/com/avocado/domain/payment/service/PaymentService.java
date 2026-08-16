package com.avocado.domain.payment.service;

import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface PaymentService {

    PaymentQrTokenResponseDto issuePaymentQrToken(AuthUser authUser);

    PaymentQrTokenResponseDto reissuePaymentQrToken(AuthUser authUser);

    PaymentSimulationResponseDto simulatePayment(
            PaymentSimulationRequestDto request
    );
}
