package com.avocado.domain.notification.service;

import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import com.avocado.domain.notification.repository.EmitterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

    // SSE 연결의 최대 유지 시간: 30분
    private static final long SSE_TIMEOUT = 30 * 60 * 1000L;
    // SSE 연결이 끊겼을 때 재연결할 시간: 3초
    private static final long RECONNECT_TIME = 3000L;
    // 연결 이벤트 이름
    private static final String CONNECT_EVENT_NAME = "connect";
    // 알림 이벤트 이름
    private static final String NOTIFICATION_EVENT_NAME = "notification";

    private final EmitterRepository emitterRepository;

    /**
     * 사용자의 SSE 연결을 생성하고 등록한다.
     *
     * @param userId 사용자 ID
     * @return 생성된 SSE 연결
     */
    public SseEmitter subscribe(
            Long userId
    ) {
        // SSE 연결을 구분하기 위한 고유 ID
        String emitterId = UUID.randomUUID().toString();

        // 실제 SSE 연결 객체 생성
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        // SSE 연결을 저장
        emitterRepository.save(
                userId,
                emitterId,
                emitter
        );

        // 정상적으로 연결이 종료된 경우
        emitter.onCompletion(
                () -> emitterRepository.delete(userId, emitterId)
        );

        // 연결 시간이 초과된 경우
        emitter.onTimeout(
                () -> {
                    emitterRepository.delete(userId, emitterId);
                    emitter.complete();
                }
        );

        // 에러가 발생한 경우
        emitter.onError(
                error -> emitterRepository.delete(userId, emitterId)
        );

        try {
            emitter.send(
                    SseEmitter.event()
                            .name(CONNECT_EVENT_NAME)
                            .data("connected")
                            .reconnectTime(RECONNECT_TIME)
            );
        } catch (IOException e) {
            emitterRepository.delete(userId, emitterId);
        }

        return emitter;
    }

    /**
     * 특정 사용자에게 알림을 전송한다.
     *
     * @param userId          수신 사용자 ID
     * @param notificationDto 알림 데이터
     */
    public void send(
            Long userId,
            NotificationResponseDto notification
    ) {

        Map<String, SseEmitter> userEmitters = emitterRepository.findAllByUserId(userId);

        // String.valueOf로 null 방지
        String id = String.valueOf(notification.getId());

        userEmitters.forEach(
                (emitterId, emitter) -> {
                    try {
                        emitter.send(
                                SseEmitter.event()
                                        .id(id)
                                        .name(NOTIFICATION_EVENT_NAME)
                                        .data(notification)
                        );
                    } catch (IOException e) {
                        emitterRepository.delete(
                                userId,
                                emitterId
                        );
                    }
                }
        );
    }

    /**
     * 현재 연결된 모든 클라이언트에게 heartbeat를 전송한다.
     */
    public void heartbeat() {
        emitterRepository
                .findAll()
                .forEach((userId, userEmitters) -> {
                    userEmitters.forEach((emitterId, emitter) -> {
                        try {
                            // comment만 설정하여 연결이 살아 있다는 신호 전송
                            emitter.send(
                                    SseEmitter.event()
                                            .comment("heartbeat")
                            );
                        } catch (IOException e) {
                            emitterRepository.delete(
                                    userId,
                                    emitterId
                            );
                        }
                    });
                });
    }
}
