package com.avocado.domain.transfer.dto.request;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 아이 선불지갑 송금 요청 정보를 전달한다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WalletTransferRequestDto {
    // 수취 금융기관 코드
    @NotBlank
    private String bankCode;

    // 수취 계좌번호 또는 아보카도 지갑 번호
    @NotBlank
    private String recipientNumber;

    // 수취인 이름
    @NotBlank
    private String recipientName;

    // 송금 금액
    @NotNull
    @Positive
    private Long amount;
}
