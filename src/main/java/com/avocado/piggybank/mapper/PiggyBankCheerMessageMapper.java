package com.avocado.piggybank.mapper;

import com.avocado.piggybank.domain.PiggyBankCheerMessage;
import com.avocado.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PiggyBankCheerMessageMapper {

    int insert(PiggyBankCheerMessage cheerMessage);

    List<PiggyBankCheerMessageResponseDto> selectByPiggyBankId(@Param("piggyBankId") Long piggyBankId);

    int deleteById(@Param("messageId") Long messageId);
}