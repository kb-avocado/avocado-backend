package com.avocado.domain.transaction.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 선불지갑에서 발생하는 거래 유형을 정의한다.
 */
@Getter
@RequiredArgsConstructor
public enum WalletTransactionType {

    CHARGE("부모 계좌에서 선불지갑으로 충전"),

    CHARGE_CANCEL("충전 취소"),

    PAYMENT("선불지갑 결제"),

    PAYMENT_CANCEL("결제 취소"),

    TRANSFER_OUT("송금 출금"),

    TRANSFER_IN("송금 입금"),

    TRANSFER_CANCEL_OUT("송금 취소 출금"),

    TRANSFER_CANCEL_IN("송금 취소 입금"),

    PIGGY_BANK_DEPOSIT("선불지갑에서 저금통으로 저축"),

    PIGGY_BANK_WITHDRAWAL("저금통에서 선불지갑으로 반환");

    private final String description;
}
