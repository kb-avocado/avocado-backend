package com.avocado.domain.transfer.service;

import com.avocado.domain.account.domain.BankCode;
import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.domain.TransferRecipientVo;
import com.avocado.domain.transfer.dto.request.TransferRecipientListRequestDto;
import com.avocado.domain.transfer.dto.response.RecipientResponseDto;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.mapper.TransferRecipientMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import com.avocado.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferRecipientServiceImpl implements TransferRecipientService {

    private final TransferRecipientMapper transferRecipientMapper;

    @Override
    public RecipientResponseDto findRecipient(
            TransferRecipientSearchType searchType,
            String keyword
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        RecipientResponseDto recipient = findRecipientBySearchType(
                searchType,
                keyword
        );

        if (recipient == null) {
            throw new BusinessException(ErrorCode.TRANSFER_RECIPIENT_NOT_FOUND);
        }

        recipient.setAccountNumber(maskAccountNumber(recipient.getAccountNumber()));

        return recipient;
    }

    /**
     * 아이의 최근 송금 수취처를 페이지 단위로 조회한다.
     *
     * @param childId 아이 회원 ID
     * @param requestDto 페이지 조회 조건
     * @return 최근 송금 수취처 페이지 응답
     */
    @Override
    @Transactional
    public PageResponse<TransferRecipientResponseDto> getRecentRecipients(
            Long childId,
            TransferRecipientListRequestDto requestDto
    ) {
        // 요청받은 페이지 번호와 페이지 크기
        int page = requestDto.getPage();
        int size = requestDto.getSize();

        // 조회 시작 위치
        int offset = page * size;

        // 동일 수취처를 하나로 계산한 전체 최근 수취처 개수를 조회
        long totalElements = transferRecipientMapper.countRecentByChildId(childId);

        // 현재 페이지에 해당하는 최근 송금 수취처를 조회
        List<TransferRecipientVo> recipients = transferRecipientMapper.findRecentByChildId(
                childId,
                offset,
                size
        );

        // 조회된 VO를 프론트 응답 DTO 목록으로 변환
        List<TransferRecipientResponseDto> items = recipients.stream()
                .map(this::toResponse)
                .toList();

        // 공통 페이지 응답을 생성하여 반환
        return PageResponse.of(
                page,
                size,
                totalElements,
                items
        );
    }

    /**
     * 송금 수취처 VO를 응답 DTO로 변환한다.
     *
     * @param recipient 송금 수취처 정보
     * @return 송금 수취처 응답
     */
    private TransferRecipientResponseDto toResponse(
            TransferRecipientVo recipient
    ) {
        // 금융기관 코드에 해당하는 BankCode를 조회.
        BankCode bankCode = BankCode.fromCode(recipient.getBankCode());

        // 금융기관 이름을 포함한 수취처 응답 DTO를 생성
        return TransferRecipientResponseDto.builder()
                .recipientName(recipient.getRecipientName())
                .bankCode(bankCode.getCode())
                .bankName(bankCode.getDescription())
                .recipientNumber(recipient.getRecipientNumber())
                .build();
    }

    private RecipientResponseDto findRecipientBySearchType(
            TransferRecipientSearchType searchType,
            String keyword
    ) {
        if (searchType == TransferRecipientSearchType.USER_CODE) {
            return transferRecipientMapper.findByUserCode(keyword);
        }

        if (searchType == TransferRecipientSearchType.ACCOUNT_NUMBER) {
            return transferRecipientMapper.findByAccountNumber(keyword);
        }

        throw new BusinessException(ErrorCode.INVALID_REQUEST);
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 8) {
            return accountNumber;
        }

        String first = accountNumber.substring(0, 4);
        String last = accountNumber.substring(accountNumber.length() - 4);

        return first + "-****-" + last;
    }
}
