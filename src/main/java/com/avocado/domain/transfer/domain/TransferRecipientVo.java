package com.avocado.domain.transfer.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 아이가 최근 송금한 수취처 정보를 관리
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class TransferRecipientVo {
    // 수취인 이름
    private String recipientName;

    // 수취 금융기관 코드
    private String bankCode;

    // 수취 계좌 또는 선불지갑 번호
    private String recipientNumber;

    // 마지막 송금 일시
    private LocalDateTime transferredAt;
}
