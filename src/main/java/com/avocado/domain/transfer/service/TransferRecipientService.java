package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.request.TransferRecipientListRequestDto;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.dto.response.RecipientResponseDto;
import com.avocado.global.response.PageResponse;

public interface TransferRecipientService {
    RecipientResponseDto findRecipient(
            TransferRecipientSearchType searchType,
            String keyword
    );

    /**
     * 아이의 최근 송금 수취처를 페이지 단위로 조회한다.
     *
     * @param childId 아이 회원 ID
     * @param requestDto 페이지 조회 조건
     * @return 최근 수취처 페이지 응답
     */
    PageResponse<TransferRecipientResponseDto> getRecentRecipients(
            Long childId,
            TransferRecipientListRequestDto requestDto
    );
}
