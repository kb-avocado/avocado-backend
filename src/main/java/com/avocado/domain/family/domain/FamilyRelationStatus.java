package com.avocado.domain.family.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 부모와 자녀의 가족 연결 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum FamilyRelationStatus {

    PENDING("가족 연결 요청 대기"),

    APPROVED("가족 연결 요청 승인"),

    REJECTED("가족 연결 요청 거절"),

    CANCELED("가족 연결 요청 또는 관계 취소"),

    ACTIVE("가족 관계 활성화");

    private final String description;
}
