package com.avocado.account.service;

import com.avocado.account.domain.AccountStatus;
import com.avocado.account.domain.AccountVo;
import com.avocado.account.dto.response.AccountCreateResponse;
import com.avocado.account.mapper.AccountMapper;
import com.avocado.common.exception.BusinessException;
import com.avocado.common.response.code.ErrorCode;
import com.avocado.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountMapper accountMapper;
    private final UserMapper userMapper;

    /**
     * 활성 상태인 부모 회원의 은행 계좌를 등록한다.
     *
     * @param userId        계좌를 등록하는 부모 회원 ID
     * @param bankCode      금융기관 코드
     * @param accountNumber 등록할 실제 계좌번호
     */
    @Override
    @Transactional
    public AccountVo createAccount(
            Long userId,
            String bankCode,
            String accountNumber
    ) {
        // 계좌를 등록하려는 회원이 활성 상태의 부모인지 확인
        boolean isActiveParent = userMapper.existsActiveParentById(userId);

        // 활성 부모 회원이 아니면 계좌 등록을 중단
        if (!isActiveParent) {
            throw new BusinessException(ErrorCode.ACTIVE_PARENT_NOT_FOUND);
        }

        // 동일한 은행 코드와 계좌번호가 이미 등록됐는지 확인
        boolean isAccountExists = accountMapper.existsByBankCodeAndAccountNumber(
                bankCode,
                accountNumber
        );

        // 이미 등록된 계좌라면 중복 계좌 예외 발생
        if (isAccountExists) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        AccountVo accountVo = AccountVo.builder()
                .userId(userId)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .build();

        try {
            int insertedRows = accountMapper.insertAccount(accountVo);

            if (insertedRows != 1) {
                throw new BusinessException(ErrorCode.ACCOUNT_CREATION_FAILED);
            }

        } catch (DuplicateKeyException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_ACCOUNT);
        }

        return accountVo;
    }
}