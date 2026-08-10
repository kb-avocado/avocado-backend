package com.avocado.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalletTxItemResponseDto {

    // 거래 내역 고유 ID
    private Long transactionId;

    // 거래 유형
    private String transactionType;

    // 거래 금액
    private Long amount;

    // 거래 상대방 또는 가맹점 이름
    private String counterpartyName;

    // 거래 처리 상태
    private String status;

    // 거래 발생 일시
    private LocalDateTime createdAt;
}
