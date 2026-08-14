package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.mapper.NotificationMapper;
import com.avocado.global.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    /**
     * 알림 저장 후 생성된 PK를 포함한 이벤트가 발행되는지 확인한다.
     */
    @Test
    void create() {
        // given
        Long receiverId = 102L;

        doAnswer(invocation -> {

            NotificationVo notification = invocation.getArgument(0);

            // MyBatis useGeneratedKeys가 PK를 주입하는 상황을 재현한다.
            ReflectionTestUtils.setField(
                    notification,
                    "id",
                    351L
            );

            return 1;

        }).when(notificationMapper)
                .insert(any(NotificationVo.class));

        // when
        notificationService.create(
                receiverId,
                NotificationType.ALLOWANCE_RECEIVED,
                "10,000원이 입금되었습니다."
        );

        // then
        ArgumentCaptor<NotificationCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(
                        NotificationCreatedEvent.class
                );

        verify(notificationMapper)
                .insert(any(NotificationVo.class));

        verify(eventPublisher)
                .publishEvent(eventCaptor.capture());

        NotificationCreatedEvent event =
                eventCaptor.getValue();

        assertThat(event.getReceiverId())
                .isEqualTo(receiverId);

        assertThat(
                event.getNotification()
                        .getId()
        ).isEqualTo(351L);

        assertThat(
                event.getNotification()
                        .getType()
        ).isEqualTo(
                NotificationType.ALLOWANCE_RECEIVED
        );
    }

    /**
     * 알림 INSERT가 실패하면 예외가 발생하는지 확인한다.
     */
    @Test
    void createFailWhenInsertFails() {

        // given
        when(notificationMapper.insert(
                any(NotificationVo.class)
        )).thenReturn(0);

        // when & then
        assertThatThrownBy(() ->
                notificationService.create(
                        102L,
                        NotificationType.ALLOWANCE_RECEIVED,
                        "10,000원이 입금되었습니다."
                )
        ).isInstanceOf(BusinessException.class);

        verify(eventPublisher, never())
                .publishEvent(any());
    }

    /**
     * INSERT 성공 후 generated key가 없으면 예외가 발생하는지 확인한다.
     */
    @Test
    void createFailWhenGeneratedKeyIsNull() {

        // given
        when(notificationMapper.insert(
                any(NotificationVo.class)
        )).thenReturn(1);

        // Mock Mapper이므로 id는 null 상태로 남는다.

        // when & then
        assertThatThrownBy(() ->
                notificationService.create(
                        102L,
                        NotificationType.ALLOWANCE_RECEIVED,
                        "10,000원이 입금되었습니다."
                )
        ).isInstanceOf(BusinessException.class);

        verify(eventPublisher, never())
                .publishEvent(any());
    }
}