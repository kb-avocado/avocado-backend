package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PiggyBankCheerMessageServiceImpl implements PiggyBankCheerMessageService {

    @Override
    public PiggyBankCheerMessageResponseDto sendMessage(Long piggyBankId, PiggyBankCheerMessageCreateRequestDto request) {
        // TODO: Mapper 연동되면 (1) 저금통 존재/ACTIVE 상태 확인 (2) insert로 교체
        return PiggyBankCheerMessageResponseDto.builder()
                .cheerMessageId(1L)
                .senderName("보호자")
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Override
    public List<PiggyBankCheerMessageResponseDto> getMessages(Long piggyBankId) {
        // TODO: Mapper 연동되면 selectByPiggyBankId로 교체
        return List.of(
                PiggyBankCheerMessageResponseDto.builder()
                        .cheerMessageId(1L)
                        .senderName("엄마")
                        .message("너무 잘하고 있어!")
                        .createdAt(LocalDateTime.now())
                        .build()
        );
    }

    @Override
    public void deleteMessage(Long piggyBankId, Long messageId) {
        // TODO: Mapper 연동되면 (1) 존재 확인 (2) deleteById 호출로 교체
    }
}