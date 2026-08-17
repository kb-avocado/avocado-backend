package com.avocado.domain.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 연동 계좌의 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum AccountStatus {

    ACTIVE("정상 연동"),

    SUSPENDED("이용 정지"),

    DISCONNECTED("계좌 연동 해제");

    private final String description;
}
