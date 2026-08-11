package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;

public interface TransferRecipientService {
    TransferRecipientResponseDto findRecipient(
            TransferRecipientSearchType searchType,
            String keyword
    );
}
