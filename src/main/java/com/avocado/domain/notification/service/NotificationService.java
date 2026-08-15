package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.global.response.PageResponse;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface NotificationService {
    /**
     * 사용자에게 전달할 알림을 생성한다.
     *
     * @param receiverId 알림 수신 사용자 ID
     * @param type       알림 유형
     * @param message    알림 내용
     */
    void create(
            Long receiverId,
            NotificationType type,
            String message
    );

    /**
     * 회원이 수신한 최근 7일 알림 목록을 페이지 단위로 조회한다.
     *
     * @param userId     회원 아이디
     * @param requestDto 페이지 조회 조건
     * @return 알림 목록 페이지
     */
    PageResponse<NotificationListItemResponseDto> getNotificationList(
            Long userId,
            NotificationListRequestDto requestDto
    );
}
