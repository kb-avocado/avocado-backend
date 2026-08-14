package com.avocado.domain.notification.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class NotificationVo {
    // DB INSERT 후 MyBatis가 generated key를 여기에 넣는다.
    private Long id;

    private Long receiverId;

    private NotificationType type;

    private String title;

    private String message;

    private boolean isRead;

    private LocalDateTime createdAt;

    /**
     * 저장할 알림 정보를 생성한다.
     *
     * @param receiverId 알림 수신 사용자 ID
     * @param type       알림 유형
     * @param title      알림 제목
     * @param message    알림 내용
     * @param read       읽음 여부
     * @param createdAt  알림 생성 일시
     */
    @Builder
    public NotificationVo(
            Long receiverId,
            NotificationType type,
            String title,
            String message,
            boolean isRead,
            LocalDateTime createdAt
    ) {
        this.receiverId = receiverId;
        this.type = type;
        this.title = title;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }
}
