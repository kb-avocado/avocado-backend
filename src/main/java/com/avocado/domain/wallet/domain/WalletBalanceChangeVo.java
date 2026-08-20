package com.avocado.domain.wallet.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 선불지갑 잔액 변경 전후 정보를 관리한다.
 */
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class WalletBalanceChangeVo {

    private Long walletId;

    private Long balanceBefore;

    private Long balanceAfter;
}