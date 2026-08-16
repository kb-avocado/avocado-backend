package com.avocado.domain.payment.service;

import com.avocado.domain.payment.dto.request.PaymentSimulationRequestDto;
import com.avocado.domain.payment.dto.response.PaymentSimulationResponseDto;
import com.avocado.domain.payment.dto.response.PaymentQrTokenResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

/**
 * 아이 선불지갑 결제를 위한 QR 토큰 발급과 POS 결제 시뮬레이션을 담당하는 서비스.
 * 토큰 수명과 재발급 제한은 결제 도메인 정책으로 관리하고, 실제 지갑 잔액 반영은 WalletService에 위임한다.
 */
public interface PaymentService {

    /**
     * 로그인 사용자의 결제 QR 토큰을 신규 발급한다.
     */
    PaymentQrTokenResponseDto issuePaymentQrToken(AuthUser authUser);

    /**
     * 기존 결제 QR 토큰을 폐기하고 새 토큰을 발급한다.
     * 짧은 시간 안의 반복 재발급은 결제 도메인 정책에 따라 제한된다.
     */
    PaymentQrTokenResponseDto reissuePaymentQrToken(AuthUser authUser);

    /**
     * 관리자 POS 화면에서 전달한 QR 토큰, 가맹점, 금액으로 결제를 시뮬레이션한다.
     */
    PaymentSimulationResponseDto simulatePayment(
            PaymentSimulationRequestDto request
    );
}
