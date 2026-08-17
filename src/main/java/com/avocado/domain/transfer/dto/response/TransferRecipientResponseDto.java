package com.avocado.domain.transfer.dto.response;

import lombok.Builder;
import lombok.Getter;

/**
 * 최근 송금 수취처 정보를 반환
 */
@Getter
@Builder
public class TransferRecipientResponseDto {
    // 수취인 이름
    private String recipientName;

    // 수취 금융기관 코드
    private String bankCode;

    // 수취 금융기관 이름
    private String bankName;

    // 수취 계좌 또는 선불지갑 번호
    private String recipientNumber;
}
