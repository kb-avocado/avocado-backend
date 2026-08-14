package com.avocado.domain.family.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 부모와 자녀의 가족 관계 유형을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum FamilyRelationType {

    FATHER("아버지"),

    MOTHER("어머니");

    private final String description;
}
