package com.avocado.domain.transfer.dto.response;

import com.avocado.domain.transfer.domain.TransferResultVo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountToWalletTransferResponseDto {
    // 송금 상대방 이름
    private String counterpartyName;

    // 송금 금액
    private Long amount;

    public static AccountToWalletTransferResponseDto from(TransferResultVo result) {
        return AccountToWalletTransferResponseDto.builder()
                .counterpartyName(result.getCounterpartyName())
                .amount(result.getAmount())
                .build();
    }
}
