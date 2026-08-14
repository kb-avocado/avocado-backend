package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.dto.response.PaymentQrActiveTokenResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    private PaymentController paymentController;

    @BeforeEach
    void setUp() {
        paymentController = new PaymentController(paymentService);
    }

    @Test
    @DisplayName("POS 시뮬레이터가 결제 대기 QR 토큰 목록을 조회한다")
    void getActivePaymentQrTokens() {
        // given
        List<PaymentQrActiveTokenResponseDto> activeTokens = List.of(
                PaymentQrActiveTokenResponseDto.builder()
                        .token("active-token")
                        .expiresAt(1797220180000L)
                        .expiresIn(180L)
                        .build()
        );

        when(paymentService.getActivePaymentQrTokens()).thenReturn(activeTokens);

        // when
        ResponseEntity<ApiResponse<List<PaymentQrActiveTokenResponseDto>>> result =
                paymentController.getActivePaymentQrTokens();

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("PAY-003");
        assertThat(result.getBody().getData()).isEqualTo(activeTokens);
    }
}
