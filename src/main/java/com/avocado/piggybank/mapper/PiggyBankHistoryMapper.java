package com.avocado.piggybank.mapper;

import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PiggyBankHistoryMapper {

    List<PiggyBankDepositResponseDto> selectDepositsByPiggyBankId(@Param("piggyBankId") Long piggyBankId);
}