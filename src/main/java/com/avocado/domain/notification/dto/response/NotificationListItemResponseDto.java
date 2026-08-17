package com.avocado.domain.notification.dto.response;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationListItemResponseDto {
    // 알림 ID
    private Long id;

    // 알림 유형
    private NotificationType type;

    // 알림 제목
    private String title;

    // 알림 내용
    private String message;

    // 읽음 여부
    private boolean isRead;

    // 알림 생성 일시
    private LocalDateTime createdAt;

    public static NotificationListItemResponseDto from(
            NotificationVo notification
    ) {
        return NotificationListItemResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
