package com.avocado.domain.transaction.domain;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletHistoryVo {

    private Long id;

    private Long walletId;

    private String traceId;

    private String transactionType;

    private Long amount;

    private Long counterpartyWalletId;

    private String counterpartyName;

    private String targetBankCode;

    private String targetAccountNumber;

    private Long merchantId;

    private String memo;

    private String status;

    private String failureCode;

    private LocalDateTime createdAt;
}
