package com.avocado.domain.notification.mapper;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.domain.NotificationVo;
import com.avocado.global.config.RootConfig;
import com.avocado.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RootConfig.class, SecurityConfig.class})
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

        // useGeneratedKeys에 의해 INSERT 후 PK가 설정되어야 한다.
        assertThat(notification.getId())
                .isNotNull()
                .isPositive();
    }

}