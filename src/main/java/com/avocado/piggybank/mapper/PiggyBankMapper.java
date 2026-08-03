package com.avocado.piggybank.mapper;

import com.avocado.piggybank.domain.BonusType;
import com.avocado.piggybank.domain.PiggyBank;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PiggyBankMapper {

    PiggyBank selectById(@Param("id") Long id);

    int updateBonus(
            @Param("id") Long id,
            @Param("bonusType") BonusType bonusType,
            @Param("bonusValue") Long bonusValue
    );
}