package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.mapper.NotificationMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
}
