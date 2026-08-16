package com.avocado.domain.merchant.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 가맹점의 서비스 이용 상태를 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum MerchantStatus {
    ACTIVE("정상 운영"),

    SUSPENDED("이용 정지"),

    CLOSED("운영 종료");

    private final String description;
}
