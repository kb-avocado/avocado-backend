package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;

public interface NotificationService {
    /**
     * 사용자에게 전달할 알림을 생성한다.
     *
     * @param receiverId 알림 수신 사용자 ID
     * @param type 알림 유형
     * @param message 알림 내용
     */
    void create(
            Long receiverId,
            NotificationType type,
            String message
    );
}
