package com.avocado.domain.notification.controller;

import com.avocado.domain.notification.service.NotificationSseService;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationSseControllerTest {

    @Mock
    private NotificationSseService notificationSseService;

    @Mock
    private AuthUser authUser;

    @InjectMocks
    private NotificationSseController notificationSseController;

    /**
     * 인증 사용자 ID로 SSE 구독 서비스를 호출하는지 확인한다.
     */
    @Test
    void subscribe() {

        // given
        Long userId = 102L;

        SseEmitter emitter =
                new SseEmitter();

        when(authUser.getUserId())
                .thenReturn(userId);

        when(
                notificationSseService.subscribe(userId)
        ).thenReturn(emitter);

        // when
        SseEmitter result =
                notificationSseController.subscribe(
                        authUser
                );

        // then
        assertThat(result)
                .isSameAs(emitter);

        verify(notificationSseService)
                .subscribe(userId);
    }
}