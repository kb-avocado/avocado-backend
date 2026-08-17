package com.avocado.domain.notification.mapper;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.global.config.DataSourceConfig;
import com.avocado.global.config.LocalPropertyConfig;
import com.avocado.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(classes = {
        LocalPropertyConfig.class,
        DataSourceConfig.class,
        MyBatisConfig.class
})
@Transactional
class NotificationMapperTest {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 알림 저장 후 AUTO_INCREMENT PK가 NotificationVo에 설정되는지 확인한다.
     */
    @Test
    @DisplayName("알림 저장 매퍼 테스트")
    void insertGeneratedKey() {
        // given
        NotificationVo notification = NotificationVo.builder()
                .receiverId(102L)
                .type(NotificationType.ALLOWANCE_RECEIVED)
                .title("용돈이 도착했어요")
                .message("10,000원이 입금되었습니다.")
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();

        assertThat(notification.getId()).isNull();

        // when
        int inserted = notificationMapper.insert(notification);

        // then
        assertThat(inserted).isEqualTo(1);

        assertThat(notification.getId())
                .isNotNull()
                .isPositive();
    }

    /**
     * 회원의 최근 7일 알림 개수를 조회한다.
     */
    @Test
    @DisplayName("회원의 최근 7일 알림 개수 조회")
    void countRecentByUserId() {
        // given
        Long userId = 101L;

        // when
        long count = notificationMapper.countRecentByUserId(userId);

        // then
        assertThat(count).isEqualTo(3L);
    }

    /**
     * 회원의 최근 7일 알림 목록을 최신순으로 조회한다.
     */
    @Test
    @DisplayName("회원의 최근 7일 알림 목록 조회")
    void findRecentByUserId() {
        // given
        Long userId = 101L;
        int offset = 0;
        int size = 20;

        // when
        List<NotificationVo> notifications =
                notificationMapper.findRecentByUserId(
                        userId,
                        offset,
                        size
                );

        // then
        assertThat(notifications).hasSize(3);

        assertThat(notifications.get(0).getReceiverId())
                .isEqualTo(userId);
    }
}