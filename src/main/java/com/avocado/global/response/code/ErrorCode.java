package com.avocado.global.response.code;

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

    // 사용자 (USR)
    CHILD_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-001", "자녀를 찾을 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-002", "사용자를 찾을 수 없습니다."),

    // 저금통 (PIG)
    PIGGY_BANK_NOT_FOUND(HttpStatus.NOT_FOUND, "PIG-001", "저금통을 찾을 수 없습니다."),
    PIGGY_BANK_BONUS_NOT_SET(HttpStatus.BAD_REQUEST, "PIG-002", "설정된 보너스가 없는 저금통입니다."),
    PIGGY_BANK_GOAL_NOT_ACHIEVED(HttpStatus.BAD_REQUEST, "PIG-003", "목표를 아직 달성하지 못했습니다."),
    PIGGY_BANK_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "PIG-004", "저금 목표는 최대 3개까지 만들 수 있습니다."),
    PIGGY_BANK_ALREADY_CLOSED(HttpStatus.CONFLICT, "PIG-005", "이미 종료된 저금통입니다."),
    PIGGY_BANK_BONUS_ALREADY_SET(HttpStatus.CONFLICT, "PIG-006", "이미 보너스가 설정된 저금통입니다."),
    PIGGY_BANK_NOT_ACTIVE(HttpStatus.CONFLICT, "PIG-007", "진행 중인 저금통이 아닙니다."),
    PIGGY_BANK_DEPOSIT_EXCEEDS_TARGET(HttpStatus.BAD_REQUEST, "PIG-008", "입금 금액이 저금통의 남은 목표 금액을 초과합니다."),
    PIGGY_BANK_BONUS_ALREADY_PAID(HttpStatus.CONFLICT, "PIG-009", "이미 지급된 보너스입니다."),

    // 선불지갑 (WAL)
    WALLET_NOT_FOUND(HttpStatus.NOT_FOUND, "WAL-001", "선불지갑을 찾을 수 없습니다."),
    WALLET_INACTIVE(HttpStatus.CONFLICT, "WAL-002", "사용할 수 없는 선불지갑입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.CONFLICT, "WAL-003", "선불지갑 잔액이 부족합니다."),
    WALLET_ALREADY_EXISTS(HttpStatus.CONFLICT, "WAL-004", "이미 선불지갑이 있는 계정입니다."),
    WALLET_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WAL-005", "선불지갑 잔액 변경에 실패했습니다."),
    WALLET_HISTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WAL-006", "선불지갑 거래 이력 저장에 실패했습니다."),
    WALLET_LEDGER_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "WAL-007", "선불지갑 원장 저장에 실패했습니다."),

    // 계좌 (ACC)
    ACTIVE_PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACC-001", "활성 상태인 부모 회원을 찾을 수 없습니다."),
    DUPLICATE_ACCOUNT(HttpStatus.CONFLICT, "ACC-002", "이미 등록된 계좌입니다."),
    ACCOUNT_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ACC-003", "계좌 등록에 실패했습니다."),
    PARENT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACC-004", "보호자 회원을 찾을 수 없습니다."),
    ACCOUNT_HISTORY_CREATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "ACC-005", "계좌 거래 이력 저장에 실패했습니다."),

    // 가맹점 (MER)
    MERCHANT_NOT_FOUND(HttpStatus.NOT_FOUND, "MER-001", "가맹점을 찾을 수 없습니다."),
    MERCHANT_INACTIVE(HttpStatus.CONFLICT, "MER-002", "현재 이용할 수 없는 가맹점입니다."),
    MERCHANT_RESTRICTED(HttpStatus.FORBIDDEN, "MER-003", "미성년자가 이용할 수 없는 가맹점입니다."),

    // 결제 (PAY)
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY-001", "결제 정보를 찾을 수 없습니다."),
    DUPLICATE_PAYMENT_REQUEST(HttpStatus.CONFLICT, "PAY-002", "이미 처리된 결제 요청입니다."),
    PAYMENT_QR_REISSUE_TOO_FREQUENT(HttpStatus.TOO_MANY_REQUESTS, "PAY-003", "QR 토큰 재발급 요청이 너무 잦습니다. 잠시 후 다시 시도해주세요."),
    INVALID_OR_EXPIRED_QR(HttpStatus.BAD_REQUEST, "PAY-101", "만료되었거나 올바르지 않은 QR 토큰입니다."),
    FORCED_FAILURE(HttpStatus.CONFLICT, "PAY-102", "POS 시뮬레이터 임의 실패입니다."),

    // 신문 (NWS)
    NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, "NWS-001", "신문 기사를 찾을 수 없습니다."),

    // 송금 (TRF)
    TRANSFER_RECIPIENT_NOT_FOUND(HttpStatus.NOT_FOUND, "TRF-001", "송금 대상을 찾을 수 없습니다."),

    // 거래 (TXN)
    WALLET_TX_NOT_FOUND(HttpStatus.NOT_FOUND, "TXN-001", "선불지갑 거래 내역을 찾을 수 없습니다."),

    // 회원 (USR)
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "USR-001", "이미 가입된 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "USR-002", "이미 가입된 전화번호입니다."),

    // 가족 연결 (FAM)
    FAMILY_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "FAM-001", "가족 연결 요청을 찾을 수 없습니다."),
    INVITE_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "FAM-002", "초대 코드와 일치하는 보호자가 없습니다."),
    CHILD_ONLY_FAMILY_REQUEST(HttpStatus.FORBIDDEN, "FAM-003", "아이 계정만 가족 연결을 요청할 수 있습니다."),
    ALREADY_CONNECTED(HttpStatus.CONFLICT, "FAM-004", "이미 보호자와 연결된 계정입니다."),
    // 보호자가 이미 승인/거절한 요청을 다시 처리하려는 경우
    FAMILY_REQUEST_ALREADY_HANDLED(HttpStatus.CONFLICT, "FAM-005", "이미 처리된 요청입니다."),
    // 보호자 승인 전에 아이가 확정하려는 경우
    FAMILY_REQUEST_NOT_APPROVED(HttpStatus.CONFLICT, "FAM-006", "보호자가 아직 승인하지 않은 요청입니다."),
    FAMILY_RELATION_NOT_FOUND(HttpStatus.NOT_FOUND, "FAM-007", "가족 관계를 확인할 수 없습니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;
}
