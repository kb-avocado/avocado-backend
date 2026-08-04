package com.avocado.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_REQUEST(
            HttpStatus.BAD_REQUEST,
            "INVALID_REQUEST",
            "요청 값이 올바르지 않습니다."
    ),

    INTERNAL_SERVER_ERROR(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "INTERNAL_SERVER_ERROR",
            "서버 내부 오류가 발생했습니다."
    ),

    // 저금통
    PIGGY_BANK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PIGGY_BANK_NOT_FOUND",
            "저금통을 찾을 수 없습니다."
    ),

    // 선불지갑
    WALLET_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "WALLET_NOT_FOUND",
            "선불지갑을 찾을 수 없습니다."
    ),

    WALLET_INACTIVE(
            HttpStatus.CONFLICT,
            "WALLET_INACTIVE",
            "사용할 수 없는 선불지갑입니다."
    ),

    INSUFFICIENT_BALANCE(
            HttpStatus.CONFLICT,
            "INSUFFICIENT_BALANCE",
            "선불지갑 잔액이 부족합니다."
    ),

    // 가맹점
    MERCHANT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "MERCHANT_NOT_FOUND",
            "가맹점을 찾을 수 없습니다."
    ),

    MERCHANT_INACTIVE(
            HttpStatus.CONFLICT,
            "MERCHANT_INACTIVE",
            "현재 이용할 수 없는 가맹점입니다."
    ),

    MERCHANT_RESTRICTED(
            HttpStatus.FORBIDDEN,
            "MERCHANT_RESTRICTED",
            "미성년자가 이용할 수 없는 가맹점입니다."
    ),

    // 결제
    PAYMENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PAYMENT_NOT_FOUND",
            "결제 정보를 찾을 수 없습니다."
    ),

    DUPLICATE_PAYMENT_REQUEST(
            HttpStatus.CONFLICT,
            "DUPLICATE_PAYMENT_REQUEST",
            "이미 처리된 결제 요청입니다."
    ),

    //신문
    NEWS_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "NEWS_NOT_FOUND",
            "신문 기사를 찾을 수 없습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
