package com.avocado.domain.wallet.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 선불지갑의 이용 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum WalletStatus {
    ACTIVE("사용 가능"),

    SUSPENDED("사용 정지"),

    CLOSED("해지");

    private final String description;
}
