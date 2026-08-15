package com.avocado.domain.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 시스템 역할을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum UserRole {
    USER("일반 사용자"),

    ADMIN("관리자");

    private final String description;
}
