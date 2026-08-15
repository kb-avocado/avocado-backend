package com.avocado.domain.notification.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class EmitterRepositoryTest {

    private EmitterRepository emitterRepository;

    @BeforeEach
    void setUp() {
        emitterRepository =
                new EmitterRepository();
    }

    /**
     * 동일 사용자의 여러 SSE 연결을 모두 저장할 수 있는지 확인한다.
     */
    @Test
    void saveMultipleEmitters() {

        // given
        Long userId = 102L;

        SseEmitter emitter1 =
                new SseEmitter();

        SseEmitter emitter2 =
                new SseEmitter();

        // when
        emitterRepository.save(
                userId,
                "emitter-1",
                emitter1
        );

        emitterRepository.save(
                userId,
                "emitter-2",
                emitter2
        );

        // then
        Map<String, SseEmitter> emitters =
                emitterRepository.findAllByUserId(userId);

        assertThat(emitters)
                .hasSize(2);

        assertThat(emitters.get("emitter-1"))
                .isSameAs(emitter1);

        assertThat(emitters.get("emitter-2"))
                .isSameAs(emitter2);
    }

    /**
     * 특정 SSE 연결만 제거되는지 확인한다.
     */
    @Test
    void deleteEmitter() {

        // given
        Long userId = 102L;

        SseEmitter emitter1 =
                new SseEmitter();

        SseEmitter emitter2 =
                new SseEmitter();

        emitterRepository.save(
                userId,
                "emitter-1",
                emitter1
        );

        emitterRepository.save(
                userId,
                "emitter-2",
                emitter2
        );

        // when
        emitterRepository.delete(
                userId,
                "emitter-1"
        );

        // then
        Map<String, SseEmitter> emitters =
                emitterRepository.findAllByUserId(userId);

        assertThat(emitters)
                .hasSize(1)
                .containsKey("emitter-2")
                .doesNotContainKey("emitter-1");
    }

    /**
     * 마지막 SSE 연결이 제거되면 사용자 entry도 제거되는지 확인한다.
     */
    @Test
    void deleteLastEmitter() {

        // given
        Long userId = 102L;

        emitterRepository.save(
                userId,
                "emitter-1",
                new SseEmitter()
        );

        // when
        emitterRepository.delete(
                userId,
                "emitter-1"
        );

        // then
        assertThat(
                emitterRepository.findAllByUserId(userId)
        ).isEmpty();
    }
}