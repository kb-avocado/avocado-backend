package com.avocado.domain.account.dto.response;

import com.avocado.domain.account.domain.AccountVo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AccountCreateResponse {

    // 생성된 계좌의 고유 식별자
    private Long accountId;

    // 계좌가 속한 금융기관 코드
    private String bankCode;

    // 일부 숫자를 숨긴 계좌번호
    private String accountNumber;

    // 생성된 계좌의 현재 상태
    private String status;

    // 정적 팩토리 메서드
    public static AccountCreateResponse from(AccountVo vo) {
        return new AccountCreateResponse(
                vo.getId(),
                vo.getBankCode(),
                vo.getAccountNumber(),
                vo.getStatus()
        );
    }
}