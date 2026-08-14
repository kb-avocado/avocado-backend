package com.avocado.domain.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 알림이 발생한 비즈니스 영역을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    WALLET("지갑 알림"),

    PIGGY_BANK("저금통 알림"),

    NEWS("경제 신문 알림"),

    SYSTEM("시스템/공지 알림");

    private final String description;
}
