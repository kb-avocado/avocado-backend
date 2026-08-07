package com.avocado.account.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AccountStatus {
    ACTIVE("정상 이용"),
    SUSPENDED("이용 정지"),
    DISCONNECTED("이용 해제");

    private final String description;
}
