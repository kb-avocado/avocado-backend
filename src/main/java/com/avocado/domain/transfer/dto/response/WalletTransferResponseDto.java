package com.avocado.domain.transfer.dto.response;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 아이 선불지갑 송금 결과를 전달한다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletTransferResponseDto {
    // 수취인 이름
    private String recipientName;

    // 수취 금융기관 코드
    private String bankCode;

    // 수취 계좌번호 또는 선불지갑 번호
    private String recipientNumber;

    // 송금 금액
    private Long amount;

    // 송금 완료 후 송금자 선불지갑 잔액
    private Long balance;

    // 송금 처리 일시
    private LocalDateTime transferredAt;
}
