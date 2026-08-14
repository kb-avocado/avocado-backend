package com.avocado.domain.notification.scheduler;

import com.avocado.domain.notification.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationHeartbeatScheduler {
    // 하트비트 전송 주기: 30초
    private static final long HEARTBEAT_INTERVAL = 30000L;

    private final NotificationSseService notificationSseService;

    /**
     * SSE 연결 유지를 위해 주기적으로 heartbeat를 전송한다.
     */
    @Scheduled(fixedDelay = HEARTBEAT_INTERVAL)
    public void sendHeartbeat() {
        notificationSseService.heartbeat();
    }
}
