package com.avocado.domain.transaction.service;

import com.avocado.domain.transaction.mapper.AccountTxMapper;
import com.avocado.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.avocado.global.response.code.ErrorCode.ACCOUNT_HISTORY_CREATE_FAILED;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountTxServiceImpl implements AccountTxService {

    private final AccountTxMapper accountTxMapper;

    @Override
    @Transactional
    public void recordWalletCharge(
            Long accountId,
            String traceId,
            Long amount
    ) {
        int insertedRows = accountTxMapper.insertWalletChargeHistory(
                accountId,
                traceId,
                amount
        );

        if (insertedRows != 1) {
            throw new BusinessException(ACCOUNT_HISTORY_CREATE_FAILED);
        }
    }
}
