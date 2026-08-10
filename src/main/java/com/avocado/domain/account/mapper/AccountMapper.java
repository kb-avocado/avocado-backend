package com.avocado.domain.account.mapper;

import com.avocado.domain.account.domain.AccountVo;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface AccountMapper {

    // 은행 코드와 계좌 번호로 계좌 중복 여부 체크
    boolean existsByBankCodeAndAccountNumber(
            @Param("bankCode") String bankCode,
            @Param("accountNumber") String accountNumber
    );

    // 부모 회원의 은행 계좌 정보를 등록하고, 저장된 행의 개수를 반환
    int insertAccount(AccountVo accountVo);

    // 부모에게 연결된 ACTIVE 외부 계좌를 조회한다.
    Optional<AccountVo> findActiveByUserId(
            @Param("userId") Long userId
    );

    // 부모의 외부 연동 계좌를 이용한 아이 지갑 충전 사실을 기록한다.
    int insertWalletChargeHistory(
            @Param("accountId") Long accountId,
            @Param("traceId") String traceId,
            @Param("amount") Long amount
    );
}
