package com.avocado.domain.account.service;

import com.avocado.domain.account.domain.AccountVo;

public interface AccountService {
    /**
     * 부모 회원의 오픈뱅킹 계좌를 등록한다.
     *
     * @param userId        계좌를 등록할 부모 회원 ID
     * @param bankCode      금융기관 코드
     * @param accountNumber 실제 은행 계좌번호
     */
    AccountVo createAccount(
            Long userId,
            String bankCode,
            String accountNumber
    );


}
