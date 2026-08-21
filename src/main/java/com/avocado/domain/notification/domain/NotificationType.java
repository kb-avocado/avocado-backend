package com.avocado.domain.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림이 발생한 비즈니스 영역을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    ALLOWANCE_RECEIVED("용돈 도착"),

    FAMILY_INVITE_RECEIVED("가족 연결 요청"),

    FAMILY_RELATION_APPROVED("가족 연결 완료"),

    SPENDING_REPORT_CREATED("리포트 도착"),

    CHEER_MESSAGE_RECEIVED("응원 메시지 도착"),

    PIGGY_BANK_ACHIEVED("저금통 모으기 성공"),

    PIGGY_BANK_BONUS_SET("보호자의 저금통 보너스 설정"),

    PIGGY_BANK_REFUNDED("저금통 환급"),

    PIGGY_BANK_CREATED("새로운 저금통"),

    PIGGY_BANK_BONUS_REMINDER("보너스 지급 대기"),

    NEWS_ACTIVITY_COMPLETED("신문 활동 완료"),

    PAYMENT_HIGH_AMOUNT("고액 결제"),

    PAYMENT_RESTRICTED_MERCHANT("유해 결제 차단");

    private final String title;
}