package com.avocado.account.mapper;

import com.avocado.account.domain.AccountVo;
import org.apache.ibatis.annotations.Param;

public interface AccountMapper {

    // 은행 코드와 계좌 번호로 계좌 중복 여부 체크
    boolean existsByBankCodeAndAccountNumber(
            @Param("bankCode") String bankCode,
            @Param("accountNumber") String accountNumber
    );

    // 부모 회원의 은행 계좌 정보를 등록하고, 저장된 행의 개수를 반환
    int insertAccount(AccountVo accountVo);


}
