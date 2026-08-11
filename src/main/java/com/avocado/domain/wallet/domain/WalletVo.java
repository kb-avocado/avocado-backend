package com.avocado.domain.wallet.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WalletVo {
    private Long id;
    private Long childId;
    private String walletNumber;
    private Long balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
