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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentAdminControllerTest {

    @Mock
    private PaymentService paymentService;

    private PaymentAdminController paymentAdminController;

    @BeforeEach
    void setUp() {
        paymentAdminController = new PaymentAdminController(paymentService);
    }

    @Test
    @DisplayName("POS 시뮬레이터가 관리자 API로 결제 대기 QR 토큰 목록을 조회한다")
    void getActivePaymentQrTokens() {
        // given
        List<PaymentQrActiveTokenResponseDto> activeTokens = List.of(
                PaymentQrActiveTokenResponseDto.builder()
                        .token("active-token")
                        .userId(102L)
                        .expiresIn(180L)
                        .build()
        );

        when(paymentService.getActivePaymentQrTokens()).thenReturn(activeTokens);

        // when
        ResponseEntity<ApiResponse<List<PaymentQrActiveTokenResponseDto>>> result =
                paymentAdminController.getActivePaymentQrTokens();

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getCode()).isEqualTo("PAY-004");
        assertThat(result.getBody().getData()).isEqualTo(activeTokens);
        verify(paymentService).getActivePaymentQrTokens();
    }

    @Test
    @DisplayName("관리자 QR 토큰 목록 API 경로를 제공한다")
    void getActivePaymentQrTokens_mapping() throws Exception {
        // when
        RequestMapping classMapping = PaymentAdminController.class.getAnnotation(RequestMapping.class);
        Method method = PaymentAdminController.class.getDeclaredMethod("getActivePaymentQrTokens");
        GetMapping methodMapping = method.getAnnotation(GetMapping.class);

        // then
        assertThat(classMapping.value()).containsExactly("/api/admin/payments");
        assertThat(methodMapping.value()).containsExactly("/qr-tokens");
    }
}
