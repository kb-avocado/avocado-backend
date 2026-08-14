package com.avocado.domain.notification.listener;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.service.NotificationSseService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationSseService notificationSseService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    /**
     * 알림 생성 이벤트 수신 시 SSE 전송 서비스를 호출하는지 확인한다.
     */
    @Test
    void handle() {

        // given
        NotificationResponseDto notification =
                NotificationResponseDto.builder()
                        .id(351L)
                        .type(NotificationType.ALLOWANCE_RECEIVED)
                        .title("용돈이 도착했어요")
                        .message("10,000원이 입금되었습니다.")
                        .createdAt(LocalDateTime.now())
                        .build();

        NotificationCreatedEvent event =
                new NotificationCreatedEvent(
                        102L,
                        notification
                );

        // when
        notificationEventListener.handle(event);

        // then
        verify(notificationSseService)
                .send(
                        102L,
                        notification
                );
    }
}