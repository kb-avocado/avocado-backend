package com.avocado.domain.transfer.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Getter
@NoArgsConstructor
public class AccountToWalletTransferRequestDto {

    // 충전을 수행하는 부모 회원 ID
    @NotNull
    private Long parentId;

    // 충전 대상 아이 회원 ID
    @NotNull
    private Long childId;

    // 충전할 금액
    @NotNull
    @Positive
    private Long amount;
}
