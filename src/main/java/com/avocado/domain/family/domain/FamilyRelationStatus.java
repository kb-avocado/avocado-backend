package com.avocado.domain.family.domain;

/**
 * 가족 연결 요청의 진행 상태.
 * 아이가 요청하면 PENDING, 보호자가 답하면 APPROVED/REJECTED,
 * 아이가 마지막으로 확정하면 ACTIVE/CANCELED가 된다.
 */
public enum FamilyRelationStatus {
    // 아이가 요청함. 보호자 응답 대기 중
    PENDING,
    // 보호자가 승인함. 아이의 최종 확인 대기 중
    APPROVED,
    // 보호자가 거절함
    REJECTED,
    // 아이가 최종 확인에서 거절
    CANCELED,
    // 연결이 확정됨
    ACTIVE
}
