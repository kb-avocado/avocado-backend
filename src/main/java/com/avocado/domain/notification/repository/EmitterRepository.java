package com.avocado.domain.notification.repository;

import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class EmitterRepository {

    private final Map<Long, Map<String, SseEmitter>> emitters = new ConcurrentHashMap<>();

    /**
     * 사용자의 SSE 연결을 저장한다.
     *
     * @param userId    사용자 ID
     * @param emitterId SSE 연결 식별자
     * @param emitter   SSE 연결 객체
     */
    public void save(
            Long userId,
            String emitterId,
            SseEmitter emitter
    ) {
        emitters
                // 해당 키가 없을 경우만 해시 맵 생성
                .computeIfAbsent(userId, key -> new ConcurrentHashMap<>())
                // 생성한 해시 맵에 키:값 저장
                .put(emitterId, emitter);
    }

    /**
     * 특정 사용자의 모든 SSE 연결을 조회한다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 SSE 연결 목록
     */
    public Map<String, SseEmitter> findAllByUserId(
            Long userId
    ) {
        // 해당 사용자의 모든 연결을 조회
        Map<String, SseEmitter> userEmitters = emitters.get(userId);

        // 키가 존재하지 않을 경우
        if (userEmitters == null) {
            return Collections.emptyMap();
        }

        // 외부에서 수정할 수 없도록 복사본 반환
        return Map.copyOf(userEmitters);
    }

    /**
     * 현재 서버가 관리하는 모든 SSE 연결을 조회한다.
     *
     * @return 전체 SSE 연결 목록
     */
    public Map<Long, Map<String, SseEmitter>> findAll() {
        // 전체 SSE 연결 정보를 담아 반환할 새로운 Map
        Map<Long, Map<String, SseEmitter>> result = new HashMap<>();

        // 저장된 모든 사용자의 SSE 연결을 순회
        emitters.forEach(
                (userId, userEmitters) -> result.put(userId, Map.copyOf(userEmitters))
        );

        // 전체 사용자의 SSE 연결 정보를 반환(수정 불가)
        return Map.copyOf(result);
    }

    /**
     * 특정 SSE 연결을 제거한다.
     *
     * @param userId    사용자 ID
     * @param emitterId SSE 연결 식별자
     */
    public void delete(
            Long userId,
            String emitterId
    ) {
        // 전체 SSE 저장소에서 해당 사용자의 연결 목록을 조회
        Map<String, SseEmitter> userEmitters = emitters.get(userId);

        // 저장소에 연결 목록이 없을 경우
        if (userEmitters == null) {
            return;
        }

        // 해당 사용자의 연결 중에서 특정 emitterId를 삭제
        userEmitters.remove(emitterId);

        // 사용자의 연결이 하나도 남아 있지 않을 경우
        if (userEmitters.isEmpty()) {
            emitters.remove(userId);
        }
    }
}
