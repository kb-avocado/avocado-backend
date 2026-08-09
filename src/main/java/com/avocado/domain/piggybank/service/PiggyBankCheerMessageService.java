package com.avocado.domain.piggybank.service;

import com.avocado.domain.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankCheerMessageResponseDto;

import java.util.List;

public interface PiggyBankCheerMessageService {

    PiggyBankCheerMessageResponseDto sendMessage(Long piggyBankId, PiggyBankCheerMessageCreateRequestDto request);

    List<PiggyBankCheerMessageResponseDto> getMessages(Long piggyBankId);

    void deleteMessage(Long piggyBankId, Long messageId);
}