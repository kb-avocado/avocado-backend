package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import com.avocado.domain.notification.dto.response.NotificationUnreadCountResponseDto;
import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.mapper.NotificationMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import com.avocado.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * 알림을 저장하고 알림 생성 이벤트를 발행한다.
     *
     * @param receiverId 알림 수신 사용자 ID
     * @param type       알림 유형
     * @param message    알림 내용
     */
    @Override
    @Transactional
    public void create(
            Long receiverId,
            NotificationType type,
            String message
    ) {
        // DB에 저장할 알림 객체 생성
        NotificationVo notification = NotificationVo.builder()
                .receiverId(receiverId)
                .type(type)
                .title(type.getTitle())
                .message(message)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        // 알림을 DB에 저장
        int inserted = notificationMapper.insert(notification);

        // 알림 저장에 실패했을 경우
        if (inserted != 1) {
            throw new BusinessException(NOTIFICATION_CREATE_FAILED);
        }

        // useGeneratedKeys ID 부여에 실패했을 경우
        if (notification.getId() == null) {
            throw new BusinessException(NOTIFICATION_CREATE_FAILED);
        }

        // DB에서 생성된 ID를 포함한 DTO를 생성
        NotificationResponseDto response = NotificationResponseDto.from(notification);

        eventPublisher.publishEvent(
                new NotificationCreatedEvent(
                        receiverId,
                        response
                )
        );
    }

    /**
     * 회원이 수신한 최근 7일 알림 목록을 페이지 단위로 조회한다.
     *
     * @param userId     회원 아이디
     * @param requestDto 페이지 조회 조건
     * @return 알림 목록 페이지
     */
    @Override
    public PageResponse<NotificationListItemResponseDto> getNotificationList(
            Long userId,
            NotificationListRequestDto requestDto
    ) {
        // 요청한 페이지 번호와 페이지 크기 조회
        int page = requestDto.getPage();
        int size = requestDto.getSize();

        // 현재 페이지에서 조회를 시작할 offset을 계산
        int offset = page * size;

        // 최근 7일 알림의 전체 개수를 조회
        long totalElements = notificationMapper.countRecentByUserId(
                userId
        );

        // 최근 7일 알림 목록을 최신순으로 조회
        List<NotificationVo> notifications = notificationMapper.findRecentByUserId(
                userId,
                offset,
                size
        );

        // 조회된 알림을 응답 DTO로 변환
        List<NotificationListItemResponseDto> items = notifications.stream()
                .map(NotificationListItemResponseDto::from)
                .toList();

        return PageResponse.of(
                page,
                size,
                totalElements,
                items
        );
    }

    /**
     * 인증 회원이 수신한 최근 7일 미읽음 알림 개수를 조회한다.
     *
     * @param userId 인증 사용자
     * @return 미읽음 알림 개수 응답
     */
    @Override
    public NotificationUnreadCountResponseDto getUnreadCount(
            Long userId
    ) {
        // 회원이 수신한 최근 7일 이내 미읽음 알림 개수를 조회
        long count = notificationMapper.countUnreadRecentByUserId(
                userId
        );

        // 조회된 미읽음 알림 개수를 응답 DTO로 변환하여 반환
        return NotificationUnreadCountResponseDto.from(
                count
        );
    }

    /**
     * 회원이 수신한 알림을 읽음 처리한다.
     *
     * @param userId         회원 ID
     * @param notificationId 알림 ID
     * @throws BusinessException 알림을 찾을 수 없는 경우
     */
    @Override
    @Transactional
    public void readNotification(
            Long userId,
            Long notificationId
    ) {
        // 알림 ID와 회원 ID를 기준으로 최근 7일 알림을 조회
        NotificationVo notification = notificationMapper
                .findRecentByIdAndUserId(
                        notificationId,
                        userId
                )
                .orElseThrow(() -> new BusinessException(NOTIFICATION_NOT_FOUND));

        // 이미 읽은 알림이면 추가 UPDATE 없이 정상 종료
        if (notification.isRead()) {
            return;
        }

        // 읽지 않은 알림을 읽음 상태로 변경
        int updated = notificationMapper.updateReadByIdAndUserId(
                notificationId,
                userId
        );

        // // 조회 이후 정상적으로 변경되지 않았다면 서버 처리 실패
        if (updated != 1) {
            throw new BusinessException(INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 회원이 수신한 최근 7일 미읽음 알림을 모두 읽음 처리한다.
     *
     * @param userId 회원 ID
     */
    @Override
    @Transactional
    public void readAllNotifications(
            Long userId
    ) {
        // 회원이 수신한 최근 7일 미읽음 알림을 모두 읽음 상태로 변경
        notificationMapper.updateAllReadByUserId(
                userId
        );
    }
}
