package com.avocado.domain.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountVo {
    private Long id;
    private Long userId;
    private String bankCode;
    private String accountNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
