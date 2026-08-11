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

    /**
     * 부모 회원에게 연결된 ACTIVE 외부 계좌를 조회한다.
     *
     * @param parentId 부모 회원 ID
     * @return 활성 상태의 외부 연동 계좌
     */
    AccountVo getActiveAccount(Long parentId);
}
