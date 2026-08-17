package com.avocado.domain.payment.controller;

import com.avocado.domain.payment.domain.PaymentRequestedResult;
import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.service.PaymentService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminPaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @Test
    @DisplayName("POS 결제 시뮬레이션 결과를 200 OK 응답으로 반환한다")
    void simulatePayment_success() {
        // given
        AdminPaymentController controller = new AdminPaymentController(paymentService);
        PaymentSimulationRequestDto request = paymentSimulationRequest();
        PaymentSimulationResponseDto response = PaymentSimulationResponseDto.builder()
                .walletHistoryId(9001L)
                .merchantId(3001L)
                .merchantName("아보카도 편의점")
                .amount(12000L)
                .status("SUCCESS")
                .balanceAfter(36000L)
                .build();

        when(paymentService.simulatePayment(same(request))).thenReturn(response);

        // when
        ResponseEntity<ApiResponse<PaymentSimulationResponseDto>> result =
                controller.simulatePayment(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getCode()).isEqualTo("PAY-005");
        assertThat(result.getBody().getData().getWalletHistoryId()).isEqualTo(9001L);
        assertThat(result.getBody().getData().getMerchantName()).isEqualTo("아보카도 편의점");
        assertThat(result.getBody().getData().getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getBody().getData().getBalanceAfter()).isEqualTo(36000L);
    }

    @Test
    @DisplayName("결제 비즈니스 실패도 200 OK 응답의 data.status=FAILED로 반환한다")
    void simulatePayment_businessFail_returnsOk() {
        // given
        AdminPaymentController controller = new AdminPaymentController(paymentService);
        PaymentSimulationRequestDto request = paymentSimulationRequest();
        PaymentSimulationResponseDto response = PaymentSimulationResponseDto.builder()
                .walletHistoryId(9002L)
                .merchantId(3001L)
                .merchantName("아보카도 편의점")
                .amount(12000L)
                .status("FAILED")
                .failureCode(ErrorCode.INSUFFICIENT_BALANCE.name())
                .balanceAfter(8000L)
                .build();

        when(paymentService.simulatePayment(same(request))).thenReturn(response);

        // when
        ResponseEntity<ApiResponse<PaymentSimulationResponseDto>> result =
                controller.simulatePayment(request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().isSuccess()).isTrue();
        assertThat(result.getBody().getData().getStatus()).isEqualTo("FAILED");
        assertThat(result.getBody().getData().getFailureCode()).isEqualTo("INSUFFICIENT_BALANCE");
        assertThat(result.getBody().getData().getBalanceAfter()).isEqualTo(8000L);
    }

    private PaymentSimulationRequestDto paymentSimulationRequest() {
        PaymentSimulationRequestDto request = new PaymentSimulationRequestDto();
        request.setQrToken("qr-token");
        request.setMerchantId(3001L);
        request.setAmount(12000L);
        request.setRequestedResult(PaymentRequestedResult.SUCCESS);
        return request;
    }
}
