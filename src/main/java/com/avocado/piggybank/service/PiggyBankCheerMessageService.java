package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankCheerMessageResponseDto;

import java.util.List;

public interface PiggyBankCheerMessageService {

    PiggyBankCheerMessageResponseDto sendMessage(Long piggyBankId, PiggyBankCheerMessageCreateRequestDto request);

    List<PiggyBankCheerMessageResponseDto> getMessages(Long piggyBankId);

    void deleteMessage(Long piggyBankId, Long messageId);
}