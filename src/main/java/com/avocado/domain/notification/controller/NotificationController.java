package com.avocado.domain.notification.controller;

import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.PageResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

        // 알림 목록 반환
        return ResponseEntity
                .status(NOTIFICATION_LIST_FETCHED.getHttpStatus())
                .body(ApiResponse.success(NOTIFICATION_LIST_FETCHED, response));
    }

}
