package com.avocado.domain.piggybank.mapper;

import com.avocado.domain.piggybank.dto.response.PiggyBankDepositResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PiggyBankHistoryMapper {

    List<PiggyBankDepositResponseDto> selectDepositsByPiggyBankId(@Param("piggyBankId") Long piggyBankId);

    int insertDeposit(
            @Param("piggyBankId") Long piggyBankId,
            @Param("amount") Long amount,
            @Param("balanceBefore") Long balanceBefore,
            @Param("balanceAfter") Long balanceAfter,
            @Param("traceId") String traceId
    );

    // 환급(중도해지) 출금 내역 기록
    int insertWithdrawal(
            @Param("piggyBankId") Long piggyBankId,
            @Param("amount") Long amount,
            @Param("balanceBefore") Long balanceBefore,
            @Param("balanceAfter") Long balanceAfter,
            @Param("traceId") String traceId
    );
}