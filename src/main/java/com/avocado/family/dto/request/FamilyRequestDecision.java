package com.avocado.family.dto.request;

/**
 * 보호자가 가족 연결 요청에 내리는 결정.
 * 저장되는 상태값(APPROVED/REJECTED)과 이름을 구분.
 */
public enum FamilyRequestDecision {

    APPROVE,
    REJECT
}
