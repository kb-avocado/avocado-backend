package com.avocado.domain.notification.listener;

import com.avocado.domain.notification.event.NotificationCreatedEvent;
import com.avocado.domain.notification.service.NotificationSseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationSseService notificationSseService;

    /**
     * 알림 저장 트랜잭션이 커밋된 후 실시간 알림을 전송한다.
     *
     * @param event 생성된 알림 이벤트
     */
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    public void handle(
            NotificationCreatedEvent event
    ) {
        notificationSseService.send(
                event.getReceiverId(),
                event.getNotification()
        );
    }
}
