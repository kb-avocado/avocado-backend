package com.avocado.domain.wallet.domain;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 선불지갑 정보를 관리하는 VO.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WalletVo {

    private Long id;

    private Long childId;

    private String walletNumber;

    private Long balance;

    private WalletStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
