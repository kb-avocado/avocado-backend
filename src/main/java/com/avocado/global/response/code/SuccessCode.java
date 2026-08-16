package com.avocado.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum SuccessCode implements ResponseCode {
    // 공통 성공 (COM)
    OK(HttpStatus.OK, "COM-000", "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED, "COM-001", "리소스가 성공적으로 생성되었습니다."),
    NO_CONTENT(HttpStatus.NO_CONTENT, "COM-002", "성공적으로 삭제되었습니다."),

    // 인증 / 로그인 (AUT)
    LOGIN_SUCCESS(HttpStatus.OK, "AUT-000", "로그인에 성공했습니다."),
    LOGOUT_SUCCESS(HttpStatus.OK, "AUT-001", "로그아웃 되었습니다."),
    TOKEN_REFRESHED(HttpStatus.OK, "AUT-002", "토큰이 성공적으로 갱신되었습니다."),
    SIGNUP_SUCCESS(HttpStatus.CREATED, "AUT-003", "회원가입 성공"),
    ACCOUNT_LINK_REQUIRED(HttpStatus.OK, "AUT-004", "로그인에 성공했습니다. 계좌 연결이 필요합니다."),
    FAMILY_LINK_REQUIRED(HttpStatus.OK, "AUT-005", "로그인에 성공했습니다. 가족 연결이 필요합니다."),
    SESSION_FOUND(HttpStatus.OK, "AUT-006", "로그인 정보를 조회했습니다."),

    // 사용자 (USR)
    MY_PAGE_FOUND(HttpStatus.OK, "USR-000", "내 정보를 조회했습니다."),

    // 선불지갑 (WAL)
    WALLET_CHARGED(HttpStatus.OK, "WAL-000", "선불지갑이 성공적으로 충전되었습니다."),
    CHILD_WALLET_FOUND(HttpStatus.OK, "WAL-001", "아이의 선불지갑을 조회했습니다."),

    // 저금통 (PIG)
    PIGGY_BANK_CREATED(HttpStatus.CREATED, "PIG-000", "저금통이 성공적으로 생성되었습니다."),
    PIGGY_BANK_DELETED(HttpStatus.NO_CONTENT, "PIG-001", "저금통이 성공적으로 삭제되었습니다."),
    PIGGY_BANK_BONUS_SET(HttpStatus.OK, "PIG-002", "보너스가 설정되었습니다."),
    PIGGY_BANK_BONUS_PAID(HttpStatus.OK, "PIG-003", "보너스 지급 처리가 완료되었습니다."),
    PIGGY_BANK_LIST_FETCHED(HttpStatus.OK, "PIG-004", "저금통 목록 조회에 성공했습니다."),
    PIGGY_BANK_DETAIL_FETCHED(HttpStatus.OK, "PIG-005", "저금통 상세 조회에 성공했습니다."),
    PIGGY_BANK_CLOSED(HttpStatus.OK, "PIG-006", "저금통을 중도 포기했습니다."),
    PIGGY_BANK_DEPOSITED(HttpStatus.OK, "PIG-007", "저금통에 성공적으로 입금되었습니다."),
    PIGGY_BANK_FAVORITE_UPDATED(HttpStatus.OK, "PIG-008", "저금통 즐겨찾기가 변경되었습니다."),

    //홈
    HOME_FOUND(HttpStatus.OK, "HOM-000", "홈 정보를 조회했습니다."),
    // 계좌 (ACC)
    ACCOUNT_CREATED(HttpStatus.CREATED, "ACC-000", "계좌가 성공적으로 등록되었습니다."),

    // 가맹점 (MER)
    MERCHANT_REGISTERED(HttpStatus.CREATED, "MER-000", "가맹점 등록이 완료되었습니다."),

    // 결제 (PAY)
    PAYMENT_SUCCESS(HttpStatus.OK, "PAY-000", "결제가 성공적으로 완료되었습니다."),
    PAYMENT_QR_TOKEN_ISSUED(HttpStatus.CREATED, "PAY-001", "결제 QR 토큰이 발급되었습니다."),
    PAYMENT_QR_TOKEN_REISSUED(HttpStatus.OK, "PAY-002", "결제 QR 토큰이 재발급되었습니다."),
    PAYMENT_QR_ACTIVE_TOKENS_FOUND(HttpStatus.OK, "PAY-004", "결제 대기 중인 QR 토큰 목록을 조회했습니다."),
    PAYMENT_SIMULATION_PROCESSED(HttpStatus.OK, "PAY-005", "결제 시뮬레이션이 처리되었습니다."),

    // 송금 (TRF)
    TRANSFER_SUCCESS(HttpStatus.OK, "TRF-000", "송금이 완료되었습니다."),
    TRANSFER_RECIPIENT_FOUND(HttpStatus.OK, "TRF-001", "송금 대상을 조회했습니다."),
    TRANSFER_RECENT_RECIPIENTS_FOUND(HttpStatus.OK, "TRF-002", "최근 수취처를 조회했습니다."),

    // 거래(TXN)
    DEPOSIT_HISTORY_FETCHED(HttpStatus.OK, "TXN-000", "거래 내역 조회에 성공했습니다."),
    WALLET_TX_LIST_FETCHED(HttpStatus.OK, "TXN-001", "선불지갑 거래 내역 조회에 성공했습니다."),
    WALLET_TX_DETAIL_FETCHED(HttpStatus.OK, "TXN-002", "선불지갑 거래 상세 조회에 성공했습니다."),


    // 응원 메시지(CHE)
    CHEER_MESSAGE_SENT(HttpStatus.CREATED, "CHE-000", "응원 메시지가 전송되었습니다."),
    CHEER_MESSAGE_FETCHED(HttpStatus.OK, "CHE-001", "응원 메시지 조회에 성공했습니다."),
    CHEER_MESSAGE_DELETED(HttpStatus.OK, "CHE-002", "응원 메시지가 삭제되었습니다."),

    // 알림 (NOT)
    NOTIFICATION_LIST_FETCHED(HttpStatus.OK, "NOT-000", "알림 목록 조회에 성공했습니다."),
    NOTIFICATION_READ(HttpStatus.OK, "NOT-001", "알림을 읽음 처리했습니다."),
    NOTIFICATION_ALL_READ(HttpStatus.OK, "NOT-002", "모든 알림을 읽음 처리했습니다."),
    NOTIFICATION_DELETED(HttpStatus.OK, "NOT-003", "알림이 삭제되었습니다."),
    NOTIFICATION_UNREAD_COUNT_FOUND(HttpStatus.OK, "NOT-004", "미읽음 알림 개수를 조회했습니다."),
    NOTIFICATION_FOUND(HttpStatus.OK, "NOT-005", "알림을 조회했습니다."),

    // 리포트(RPT)
    REPORT_FOUND(HttpStatus.OK, "RPT-000", "월별 리포트를 조회했습니다."),
    SPENDING_REPORT_TYPE_FOUND(HttpStatus.OK, "RPT-001", "이번 달 소비 유형을 조회했습니다."),

    // 경제 신문(NWS)
    NEWS_LIST_FOUND(HttpStatus.OK, "NWS-000", "신문 목록을 조회했습니다."),
    NEWS_DETAIL_FOUND(HttpStatus.OK, "NWS-001", "신문 상세 정보를 조회했습니다."),
    NEWS_ANSWER_SAVED(HttpStatus.OK, "NWS-002", "챌린지 답변이 저장되었습니다."),

    // 가족 연결(FAM)
    FAMILY_REQUEST_CREATED(HttpStatus.CREATED, "FAM-000", "가족 연결을 요청했습니다."),
    FAMILY_REQUEST_FOUND(HttpStatus.OK, "FAM-001", "가족 연결 요청을 조회했습니다."),
    // 보호자가 승인 또는 거절한 결과. 어느 쪽인지는 응답의 status로 구분한다.
    FAMILY_REQUEST_DECIDED(HttpStatus.OK, "FAM-002", "가족 연결 요청을 처리했습니다."),
    // 아이가 최종 승인 또는 거절한 결과
    FAMILY_REQUEST_CONFIRMED(HttpStatus.OK, "FAM-003", "가족 연결 요청을 확정했습니다.");

    private final HttpStatus httpStatus;

    private final String code;

    private final String message;

}
