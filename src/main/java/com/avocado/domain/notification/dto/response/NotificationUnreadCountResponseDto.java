package com.avocado.domain.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationUnreadCountResponseDto {

    // 최근 7일 이내 미읽음 알림 개수
    private long count;

    /**
     * 미읽음 알림 개수 응답을 생성한다.
     *
     * @param count 미읽음 알림 개수
     * @return 미읽음 알림 개수 응답
     */
    public static NotificationUnreadCountResponseDto from(
            long count
    ) {
        return NotificationUnreadCountResponseDto.builder()
                .count(count)
                .build();
    }
}
