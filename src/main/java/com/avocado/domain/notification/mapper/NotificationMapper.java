package com.avocado.domain.notification.mapper;

import com.avocado.domain.notification.domain.NotificationVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

public interface NotificationMapper {
    /**
     * 알림을 저장하고 생성된 PK를 NotificationVo의 id에 설정한다.
     *
     * @param notification 저장할 알림
     * @return INSERT된 행 수
     */
    int insert(
            NotificationVo notification
    );

    /**
     * 회원이 수신한 최근 7일 알림의 전체 개수를 조회한다.
     *
     * @param userId 회원 ID
     * @return 최근 7일 알림 개수
     */
    long countRecentByUserId(
            @Param("userId") Long userId
    );

    /**
     * 회원이 수신한 최근 7일 알림 목록을 최신순으로 조회한다.
     *
     * @param userId 회원 ID
     * @param offset 조회 시작 위치
     * @param size 조회 개수
     * @return 최근 7일 알림 목록
     */
    List<NotificationVo> findRecentByUserId(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 회원이 수신한 최근 7일 미읽음 알림 개수를 조회한다.
     *
     * @param userId 회원 ID
     * @return 최근 7일 미읽음 알림 개수
     */
    long countUnreadRecentByUserId(
            @Param("userId") Long userId
    );

    /**
     * 회원이 수신한 최근 7일 알림을 ID로 조회한다.
     *
     * @param notificationId 알림 ID
     * @param userId 회원 ID
     * @return 조회된 알림
     */
    Optional<NotificationVo> findRecentByIdAndUserId(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );

    int updateReadByIdAndUserId(
            @Param("notificationId") Long notificationId,
            @Param("userId") Long userId
    );

    /**
     * 회원이 수신한 최근 7일 미읽음 알림을 모두 읽음 상태로 변경한다.
     *
     * @param userId 회원 ID
     * @return 변경된 알림 개수
     */
    int updateAllReadByUserId(
            @Param("userId") Long userId
    );
}
