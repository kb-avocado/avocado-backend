package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import com.avocado.domain.notification.repository.EmitterRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSseServiceTest {

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private SseEmitter emitter1;

    @Mock
    private SseEmitter emitter2;

    private NotificationSseService notificationSseService;

    @BeforeEach
    void setUp() {
        notificationSseService =
                new NotificationSseService(
                        emitterRepository
                );
    }

    /**
     * 사용자의 모든 SSE 연결에 알림을 전송하는지 확인한다.
     */
    @Test
    void sendToAllEmitters() throws IOException {

        // given
        Long userId = 102L;

        NotificationResponseDto notification =
                NotificationResponseDto.builder()
                        .id(351L)
                        .type(NotificationType.ALLOWANCE_RECEIVED)
                        .title("용돈이 도착했어요")
                        .message("10,000원이 입금되었습니다.")
                        .createdAt(LocalDateTime.now())
                        .build();

        when(
                emitterRepository.findAllByUserId(userId)
        ).thenReturn(
                Map.of(
                        "emitter-1", emitter1,
                        "emitter-2", emitter2
                )
        );

        // when
        notificationSseService.send(
                userId,
                notification
        );

        // then
        verify(emitter1)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );

        verify(emitter2)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );
    }

    /**
     * SSE 전송 실패 시 해당 연결을 제거하는지 확인한다.
     */
    @Test
    void deleteEmitterWhenSendFails()
            throws IOException {

        // given
        Long userId = 102L;

        NotificationResponseDto notification =
                NotificationResponseDto.builder()
                        .id(351L)
                        .type(NotificationType.ALLOWANCE_RECEIVED)
                        .title("용돈이 도착했어요")
                        .message("10,000원이 입금되었습니다.")
                        .createdAt(LocalDateTime.now())
                        .build();

        when(
                emitterRepository.findAllByUserId(userId)
        ).thenReturn(
                Map.of(
                        "emitter-1", emitter1
                )
        );

        doThrow(new IOException())
                .when(emitter1)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );

        // when
        notificationSseService.send(
                userId,
                notification
        );

        // then
        verify(emitterRepository)
                .delete(
                        userId,
                        "emitter-1"
                );
    }

    /**
     * 현재 연결된 모든 SSE emitter에 heartbeat를 전송하는지 확인한다.
     */
    @Test
    void sendHeartbeatToAllEmitters()
            throws IOException {

        // given
        when(
                emitterRepository.findAll()
        ).thenReturn(
                Map.of(
                        102L,
                        Map.of(
                                "emitter-1",
                                emitter1
                        ),
                        103L,
                        Map.of(
                                "emitter-2",
                                emitter2
                        )
                )
        );

        // when
        notificationSseService.heartbeat();

        // then
        verify(emitter1)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );

        verify(emitter2)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );
    }

    /**
     * heartbeat 전송 실패 시 해당 SSE 연결을 제거하는지 확인한다.
     */
    @Test
    void deleteEmitterWhenHeartbeatFails()
            throws IOException {

        // given
        when(
                emitterRepository.findAll()
        ).thenReturn(
                Map.of(
                        102L,
                        Map.of(
                                "emitter-1",
                                emitter1
                        )
                )
        );

        doThrow(new IOException())
                .when(emitter1)
                .send(
                        any(SseEmitter.SseEventBuilder.class)
                );

        // when
        notificationSseService.heartbeat();

        // then
        verify(emitterRepository)
                .delete(
                        102L,
                        "emitter-1"
                );
    }
}