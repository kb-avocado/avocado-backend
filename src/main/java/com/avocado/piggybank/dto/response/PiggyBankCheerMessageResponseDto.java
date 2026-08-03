package com.avocado.piggybank.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PiggyBankCheerMessageResponseDto {

    private final Long cheerMessageId;
    private final String senderName;
    private final String message;
    private final LocalDateTime createdAt;
}