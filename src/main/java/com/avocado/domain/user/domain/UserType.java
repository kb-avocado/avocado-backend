package com.avocado.domain.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 일반 회원의 사용자 유형을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum UserType {
    PARENT("부모"),

    CHILD("아이");

    private final String description;
}
