package com.avocado.domain.transaction.service;

import com.avocado.global.response.PageResponse;
import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;

public interface WalletTxService {
    /**
     * 회원의 선불지갑 거래 내역을 페이지 단위로 조회한다.
     *
     * @param userId     로그인한 회원 ID
     * @param requestDto 페이지 조회 조건
     * @return 페이지네이션이 적용된 선불지갑 거래 목록
     */
    PageResponse<WalletTxItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto requestDto
    );
}
