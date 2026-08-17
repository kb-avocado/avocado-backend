package com.avocado.domain.transfer.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TransferResultVo {
    // 송금 상대방 이름
    private final String counterpartyName;
    // 송금 금액
    private final Long amount;
}
