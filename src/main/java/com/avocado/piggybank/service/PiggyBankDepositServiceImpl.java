package com.avocado.piggybank.service;

import com.avocado.piggybank.dto.response.PiggyBankDepositResponseDto;
import com.avocado.piggybank.mapper.PiggyBankHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankDepositServiceImpl implements PiggyBankDepositService {

    private final PiggyBankHistoryMapper piggyBankHistoryMapper;

    @Override
    public List<PiggyBankDepositResponseDto> getDeposits(Long piggyBankId) {
        return piggyBankHistoryMapper.selectDepositsByPiggyBankId(piggyBankId);
    }
}