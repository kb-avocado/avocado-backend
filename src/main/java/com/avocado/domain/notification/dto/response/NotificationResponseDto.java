package com.avocado.domain.notification.dto.response;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationResponseDto {

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

    /**
     * NotificationVo를 실시간 알림 응답 DTO로 변환한다.
     *
     * @param notification 알림 정보
     * @return 실시간 알림 응답
     */
    public static NotificationResponseDto from(
            NotificationVo notification
    ) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
