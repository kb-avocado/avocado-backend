package com.avocado.domain.notification.controller;

import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.domain.notification.dto.response.NotificationUnreadCountResponseDto;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.PageResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 인증 회원이 수신한 최근 7일 알림 목록을 조회한다.
     *
     * @param authUser   인증 사용자
     * @param requestDto 페이지 조회 조건
     * @return 알림 목록 응답
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<NotificationListItemResponseDto>>> getNotificationList(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @ModelAttribute NotificationListRequestDto requestDto
    ) {

        // 회원의 최근 7일 알림 목록을 페이지 단위로 조회
        PageResponse<NotificationListItemResponseDto> response = notificationService.getNotificationList(
                authUser.getUserId(),
                requestDto
        );

        // 알림 목록 응답 반환
        return ResponseEntity
                .status(NOTIFICATION_LIST_FETCHED.getHttpStatus())
                .body(ApiResponse.success(NOTIFICATION_LIST_FETCHED, response));
    }

    /**
     * 인증 회원이 수신한 최근 7일 미읽음 알림 개수를 조회한다.
     *
     * @param authUser 인증 사용자
     * @return 미읽음 알림 개수 응답
     */
    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<NotificationUnreadCountResponseDto>> getUnreadCount(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        // 회원이 수신한 최근 7일 미읽음 알림 개수를 조회
        NotificationUnreadCountResponseDto response = notificationService.getUnreadCount(authUser.getUserId());

        // 미읽음 알림 개수 응답 반환
        return ResponseEntity
                .status(NOTIFICATION_UNREAD_COUNT_FOUND.getHttpStatus())
                .body(ApiResponse.success(NOTIFICATION_UNREAD_COUNT_FOUND, response));
    }

    /**
     * 인증 회원이 수신한 알림을 읽음 처리한다.
     *
     * @param authUser       인증 사용자
     * @param notificationId 알림 ID
     * @return 알림 읽음 처리 성공 응답
     */
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse<Void>> readNotification(
            @AuthenticationPrincipal AuthUser authUser,
            @PathVariable Long notificationId
    ) {

        // 현재 회원이 수신한 알림을 읽음 상태로 변경
        notificationService.readNotification(
                authUser.getUserId(),
                notificationId
        );

        // 별도의 응답 데이터 없이 읽음 처리 성공 응답을 반환
        return ResponseEntity
                .status(NOTIFICATION_READ.getHttpStatus())
                .body(ApiResponse.success(NOTIFICATION_READ));
    }

    /**
     * 인증 회원이 수신한 최근 7일 미읽음 알림을 모두 읽음 처리한다.
     *
     * @param authUser 인증 사용자
     * @return 전체 알림 읽음 처리 성공 응답
     */
    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> readAllNotifications(
            @AuthenticationPrincipal AuthUser authUser
    ) {

        // 현재 회원이 수신한 최근 7일 미읽음 알림을 모두 읽음 처리
        notificationService.readAllNotifications(
                authUser.getUserId()
        );

        // 별도의 응답 데이터 없이 전체 읽음 처리 성공 응답을 반환
        return ResponseEntity
                .status(NOTIFICATION_ALL_READ.getHttpStatus())
                .body(ApiResponse.success(NOTIFICATION_ALL_READ));
    }
}
