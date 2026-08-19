package com.avocado.domain.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림이 발생한 비즈니스 영역을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {

    ALLOWANCE_RECEIVED("용돈이 도착했어요"),

    FAMILY_INVITE_RECEIVED("가족 초대가 도착했어요"),

    FAMILY_RELATION_APPROVED("가족 연결이 완료되었어요"),

    SPENDING_REPORT_CREATED("이번 달 소비 리포트가 생성되었어요"),

    CHEER_MESSAGE_RECEIVED("새 응원 메시지가 도착했어요"),

    PIGGY_BANK_ACHIEVED("저금통 목표를 달성했어요"),

    PIGGY_BANK_BONUS_SET("저금통 보너스가 설정됐어요"),

    PIGGY_BANK_REFUNDED("저금통이 환급됐어요"),

    PIGGY_BANK_CREATED("새 저금통이 생겼어요"),

    PIGGY_BANK_BONUS_REMINDER("보너스 지급을 기다리고 있어요");

    private final String title;
}
