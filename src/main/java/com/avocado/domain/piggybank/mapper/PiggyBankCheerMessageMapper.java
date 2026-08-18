package com.avocado.domain.piggybank.mapper;

import com.avocado.domain.piggybank.domain.PiggyBankCheerMessage;
import com.avocado.domain.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PiggyBankCheerMessageMapper {

    int insert(PiggyBankCheerMessage cheerMessage);

    List<PiggyBankCheerMessageResponseDto> selectByPiggyBankId(@Param("piggyBankId") Long piggyBankId);

    int deleteById(@Param("messageId") Long messageId, @Param("parentId") Long parentId);

    Long selectLastInsertId();

    Long selectChildIdByPiggyBankId(@Param("piggyBankId") Long piggyBankId);
}
