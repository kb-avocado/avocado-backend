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

    // 저금통 삭제 (소유권 검증용 walletId)
    void close(Long piggyBankId, Long walletId);

    // 7일 달성 승격 (스케줄러가 호출), 승격된 저금통 수 반환
    int promoteAchievements();

    // 즐겨찾기
    boolean toggleFavorite(Long piggyBankId, Long walletId);
}
