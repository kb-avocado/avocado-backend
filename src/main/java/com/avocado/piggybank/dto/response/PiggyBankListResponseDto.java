package com.avocado.piggybank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
// 저금통 목록 조회 전체 응답
public class PiggyBankListResponseDto {

    private final List<PiggyBankResponseDto> piggyBanks; // 저금통 목록
    private final int activeCount;   // 현재 진행중 개수
    private final int maxCount;      // 만들 수 있는 최대 개수(3)
    private final boolean canCreate; // 더 만들 수 있는지 (activeCount < maxCount)

}
