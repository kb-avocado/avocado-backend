package com.avocado.domain.transfer.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.transfer.dto.request.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.mapper.TransferRecipientMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferRecipientService {

    private final TransferRecipientMapper transferRecipientMapper;

    public TransferRecipientResponseDto findRecipient(
            TransferRecipientSearchType searchType,
            String keyword
    ) {
        if (keyword == null || keyword.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }

        TransferRecipientResponseDto recipient = findRecipientBySearchType(
                searchType,
                keyword
        );

        if (recipient == null) {
            throw new BusinessException(ErrorCode.TRANSFER_RECIPIENT_NOT_FOUND);
        }

        recipient.setAccountNumber(maskAccountNumber(recipient.getAccountNumber()));

        return recipient;
    }

    private TransferRecipientResponseDto findRecipientBySearchType(
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
