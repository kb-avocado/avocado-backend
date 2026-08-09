package com.avocado.domain.transaction.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class WalletTxItemResponseDto {
    //
    private Long transactionId;

    private String transactionType;

    private Long amount;

    private String counterpartyName;

    private String status;

    private LocalDateTime createdAt;
}
