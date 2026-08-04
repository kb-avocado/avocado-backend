package com.avocado.piggybank.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class PiggyBankCheerMessage {

    private final Long id;
    private final Long piggyBankId;
    private final Long parentId;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}