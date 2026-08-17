package com.avocado.domain.piggybank.service;

import com.avocado.domain.piggybank.dto.request.PiggyBankCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankDetailResponseDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankListResponseDto;
// 저금통 비즈니스 로직 인터페이스
public interface PiggyBankService {
    /**
     * 저금통 목록 조회
     * @param walletId 지갑 ID
     * @param status   탭 구분 (IN_PROGRESS / CLOSED)
     */
    PiggyBankListResponseDto getList(Long walletId, String status);

    // 저금통 상세 조회 (소유권 검증용 walletId)
    PiggyBankDetailResponseDto getDetail(Long piggyBankId, Long walletId);

    // 저금통 생성
    PiggyBankDetailResponseDto create(Long walletId, PiggyBankCreateRequestDto request);

    // 7일 달성 승격 (스케줄러가 호출), 승격된 저금통 수 반환
    int promoteAchievements();

    // 즐겨찾기
    boolean toggleFavorite(Long piggyBankId, Long walletId);

    // 저금통 중도 포기 + 잔액 환급 (childId: 지갑 반영 대상)
    void close(Long piggyBankId, Long childId, Long walletId);
}
