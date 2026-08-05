package com.avocado.common.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode implements ResponseCode {
    // 공통 에러 (COM)
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "COM-001", "요청 값이 올바르지 않습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM-002", "서버 내부 오류가 발생했습니다."),

    // 인증 / 인가 / 로그인 (AUT)
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUT-001", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "AUT-002", "접근 권한이 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUT-003", "이메일 또는 비밀번호가 올바르지 않습니다."),
    USER_SUSPENDED(HttpStatus.FORBIDDEN, "AUT-004", "이용이 정지된 계정입니다. 고객센터에 문의해주세요."),
    USER_DELETED(HttpStatus.FORBIDDEN, "AUT-005", "탈퇴한 계정입니다."),

    // 저금통 (PIG)
    PIGGY_BANK_NOT_FOUND(HttpStatus.NOT_FOUND, "PIG-001", "저금통을 찾을 수 없습니다."),
    PIGGY_BANK_BONUS_NOT_SET(HttpStatus.BAD_REQUEST, "PIG-002", "설정된 보너스가 없는 저금통입니다."),
    PIGGY_BANK_GOAL_NOT_ACHIEVED(HttpStatus.BAD_REQUEST, "PIG-003", "목표를 아직 달성하지 못했습니다."),
    PIGGY_BANK_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "PIG-004", "저금 목표는 최대 3개까지 만들 수 있습니다."),
    PIGGY_BANK_ALREADY_CLOSED(HttpStatus.CONFLICT, "PIG-005", "이미 종료된 저금통입니다."),
    PIGGY_BANK_BONUS_ALREADY_SET(HttpStatus.CONFLICT, "PIG-006", "이미 보너스가 설정된 저금통입니다."),
    PIGGY_BANK_NOT_ACTIVE(HttpStatus.CONFLICT, "PIG-007", "진행 중인 저금통이 아닙니다."),
    PIGGY_BANK_DEPOSIT_EXCEEDS_TARGET(HttpStatus.BAD_REQUEST, "PIG-008", "입금 금액이 저금통의 남은 목표 금액을 초과합니다."),

    // 선불지갑 (WAL)
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "WAL-001", "선불지갑을 찾을 수 없습니다."),
    WALLET_INACTIVE(HttpStatus.CONFLICT, "WAL-002", "사용할 수 없는 선불지갑입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "WAL-003", "선불지갑 잔액이 부족합니다."),

    // 가맹점 (MER)
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "MER-001", "가맹점을 찾을 수 없습니다."),
    MERCHANT_INACTIVE(HttpStatus.CONFLICT, "MER-002", "현재 이용할 수 없는 가맹점입니다."),
    MERCHANT_RESTRICTED(HttpStatus.FORBIDDEN, "MER-003", "미성년자가 이용할 수 없는 가맹점입니다."),

    // 결제 (PAY)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY-001", "결제 정보를 찾을 수 없습니다."),
    DUPLICATE_PAYMENT_REQUEST(HttpStatus.CONFLICT, "PAY-002", "이미 처리된 결제 요청입니다."),

    // 신문 (NWS)
    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "NWS-001", "신문 기사를 찾을 수 없습니다."),

    // 송금 (TRF)
    TRANSFER_RECIPIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TRF-001", "송금 대상을 찾을 수 없습니다."),

    // 회원 (USR)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USR-001", "이미 가입된 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "USR-002", "이미 가입된 전화번호입니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
