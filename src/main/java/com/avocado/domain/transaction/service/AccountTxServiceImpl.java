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

    /**
     * 부모 외부 계좌에서 아이 선불지갑으로 송금한 계좌 거래 이력을 기록한다.
     *
     * @param accountId 부모 외부 계좌 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 거래 금액
     */
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

    /**
     * 아이 선불지갑에서 서비스 등록 계좌로 들어온 입금 이력을 기록한다.
     *
     * @param accountId 입금받는 부모 연동 계좌 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 입금 금액
     */
    @Override
    @Transactional
    public void recordWalletDeposit(
            Long accountId,
            String traceId,
            Long amount
    ) {
        int insertedRows = accountTxMapper.insertWalletDepositHistory(
                accountId,
                traceId,
                amount
        );

        if (insertedRows != 1) {
            throw new BusinessException(ACCOUNT_HISTORY_CREATE_FAILED);
        }
    }
}
