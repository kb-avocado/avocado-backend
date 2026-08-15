package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.domain.notification.dto.response.NotificationUnreadCountResponseDto;
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

    /**
     * 회원이 수신한 최근 7일 미읽음 알림 개수를 조회한다.
     *
     * @param userId 회원 ID
     * @return 미읽음 알림 개수 응답
     */
    NotificationUnreadCountResponseDto getUnreadCount(
            Long userId
    );

    /**
     * 회원이 수신한 알림을 읽음 처리한다.
     *
     * @param userId 회원 ID
     * @param notificationId 알림 ID
     * @throws BusinessException 알림을 찾을 수 없는 경우
     */
    void readNotification(
            Long userId,
            Long notificationId
    );
}
