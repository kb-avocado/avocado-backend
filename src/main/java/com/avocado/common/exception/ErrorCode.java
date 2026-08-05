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

    // 인증/인가
    UNAUTHORIZED(
            HttpStatus.UNAUTHORIZED,
            "UNAUTHORIZED",
            "인증이 필요합니다."
    ),

    FORBIDDEN(
            HttpStatus.FORBIDDEN,
            "FORBIDDEN",
            "접근 권한이 없습니다."
    ),

    // 로그인
    // 계정 존재 여부가 드러나지 않도록 이메일 오류와 비밀번호 오류를 같은 코드로 응답한다.
    INVALID_CREDENTIALS(
            HttpStatus.UNAUTHORIZED,
            "INVALID_CREDENTIALS",
            "이메일 또는 비밀번호가 올바르지 않습니다."
    ),

    USER_SUSPENDED(
            HttpStatus.FORBIDDEN,
            "USER_SUSPENDED",
            "이용이 정지된 계정입니다. 고객센터에 문의해주세요."
    ),

    USER_DELETED(
            HttpStatus.FORBIDDEN,
            "USER_DELETED",
            "탈퇴한 계정입니다."
    ),

    // 저금통
    PIGGY_BANK_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "PIGGY_BANK_NOT_FOUND",
            "저금통을 찾을 수 없습니다."
    ),
    // 저금통 보너스 X
    PIGGY_BANK_BONUS_NOT_SET(
            HttpStatus.BAD_REQUEST,
            "PIGGY_BANK_BONUS_NOT_SET",
            "설정된 보너스가 없는 저금통입니다."
    ),
    // 저금통 목표 미달성 시
    PIGGY_BANK_GOAL_NOT_ACHIEVED(
            HttpStatus.BAD_REQUEST,
            "PIGGY_BANK_GOAL_NOT_ACHIEVED",
            "목표를 아직 달성하지 못했습니다."
    ),
    // 저금통 생성시(최대 3개 제한)
    PIGGY_BANK_LIMIT_EXCEEDED(
            HttpStatus.CONFLICT,
            "PIGGY_BANK_LIMIT_EXCEEDED",
            "저금 목표는 최대 3개까지 만들 수 있습니다."
    ),
    // 저금통 보너스 설정 시
    PIGGY_BANK_BONUS_ALREADY_SET(
            HttpStatus.CONFLICT,
            "PIGGY_BANK_BONUS_ALREADY_SET",
            "이미 보너스가 설정된 저금통입니다."
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
    ),

    // 송금
    TRANSFER_RECIPIENT_NOT_FOUND(
            HttpStatus.NOT_FOUND,
            "TRANSFER_RECIPIENT_NOT_FOUND",
            "송금 대상을 찾을 수 없습니다."
    );

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
