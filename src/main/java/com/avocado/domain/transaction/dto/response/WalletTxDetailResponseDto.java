package com.avocado.domain.transaction.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WalletTxDetailResponseDto {

    // 거래 내역 고유 ID
    private Long transactionId;

    // 거래 추적 ID
    private String traceId;

    // 거래 유형
    private String transactionType;

    // 거래 금액
    private Long amount;

    // 거래 상대방 또는 가맹점 이름
    private String counterpartyName;

    // 타행 송금 대상 은행 코드
    private String targetBankCode;

    // 타행 송금 대상 계좌번호
    private String targetAccountNumber;

    // 거래 메모
    private String memo;

    // 거래 처리 상태
    private String status;

    // 거래 실패 코드
    private String failureCode;

    // 거래 발생 일시
    private LocalDateTime createdAt;

    // 원장 유형: IN, OUT
    private String ledgerType;

    // 거래 전 잔액
    private Long balanceBefore;

    // 거래 후 잔액
    private Long balanceAfter;

    // 결제 가맹점 카테고리
    private String merchantCategory;
}
