package com.avocado.piggybank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
// 저금통 목록/상세의 "항목 하나"에 대한 응답 DTO.
public class PiggyBankResponseDto {

    private final Long piggyBankId;   // 저금통 ID
    private final String name;        // 저금통 이름
    private final String status;      // ACTIVE, PENDING_ACHIEVE, ACHIEVE, CANCEL
    private final Boolean favorite;   // 즐겨찾기 여부
    private final Long savedAmount;   // 현재 모은 금액 (DB balance)
    private final Long targetAmount;  // 목표 금액
    private final Integer progressRate; // 달성률(%) - 서비스에서 계산
}
