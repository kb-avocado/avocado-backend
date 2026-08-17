package com.avocado.domain.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원의 계정 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum UserStatus {
    PENDING("가입 절차 진행 중"),

    ACTIVE("정상 이용 가능"),

    SUSPENDED("이용 정지"),

    DELETED("탈퇴 처리");

    private final String description;
}
