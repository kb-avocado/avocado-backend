package com.avocado.domain.notification.mapper;

import com.avocado.domain.notification.domain.NotificationVo;

public interface NotificationMapper {
    /**
     * 알림을 저장하고 생성된 PK를 NotificationVo의 id에 설정한다.
     *
     * @param notification 저장할 알림
     * @return INSERT된 행 수
     */
    int insert(NotificationVo notification);
}
