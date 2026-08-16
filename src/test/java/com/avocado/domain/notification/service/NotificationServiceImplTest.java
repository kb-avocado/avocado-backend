package com.avocado.domain.notification.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.domain.notification.dto.request.NotificationListRequestDto;
import com.avocado.domain.notification.dto.response.NotificationListItemResponseDto;
import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.mapper.NotificationMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 회원의 최근 7일 알림 목록을 페이지 단위로 조회한다.
     */
    @Test
    @DisplayName("회원 알림 목록 조회 서비스 테스트")
    void getNotificationList() {
        // given
        Long userId = 101L;

        AuthUser authUser = AuthUser.builder()
                .userId(userId)
                .build();

        NotificationListRequestDto requestDto = new NotificationListRequestDto();
        requestDto.setPage(0);
        requestDto.setSize(20);

        NotificationVo notification1 = NotificationVo.builder()
                .receiverId(userId)
                .type(NotificationType.FAMILY_RELATION_APPROVED)
                .title("가족 연결이 완료되었어요")
                .message("아이와 가족 연결이 완료되었습니다.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        NotificationVo notification2 = NotificationVo.builder()
                .receiverId(userId)
                .type(NotificationType.SPENDING_REPORT_CREATED)
                .title("이번 달 소비 리포트가 생성되었어요")
                .message("아이의 소비 리포트가 생성되었습니다.")
                .isRead(true)
                .createdAt(LocalDateTime.now().minusDays(1))
                .build();

        // 전체 알림 개수 조회 결과 설정
        when(notificationMapper.countRecentByUserId(userId))
                .thenReturn(2L);

        // 알림 목록 조회 결과 설정
        when(notificationMapper.findRecentByUserId(
                userId,
                0,
                20
        )).thenReturn(List.of(
                notification1,
                notification2
        ));

        // when
        PageResponse<NotificationListItemResponseDto> response =
                notificationService.getNotificationList(
                        authUser.getUserId(),
                        requestDto
                );

        // then
        assertThat(response.getPage()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
        assertThat(response.getTotalElements()).isEqualTo(2L);
        assertThat(response.getItems()).hasSize(2);

        // Mapper 호출값 검증
        verify(notificationMapper).countRecentByUserId(userId);

        verify(notificationMapper).findRecentByUserId(
                userId,
                0,
                20
        );
    }
}