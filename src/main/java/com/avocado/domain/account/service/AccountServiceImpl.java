package com.avocado.domain.account.service;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.mapper.AccountMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.mapper.UserMapper;
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
     * 부모 회원의 은행 계좌를 등록한다.
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
        // 계좌를 등록하려는 회원의 상태를 확인. 부모 회원이 아니면 null이다.
        UserStatus status = userMapper.selectParentStatusById(userId);

        // 부모 회원이 아니면 계좌 등록을 중단
        if (status == null) {
            throw new BusinessException(ErrorCode.PARENT_NOT_FOUND);
        }

        // 계좌 등록은 가입 직후(PENDING)와 이미 계좌를 가진(ACTIVE) 부모에게만 허용한다.
        if (status == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }

        if (status == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_DELETED);
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

        if (status == UserStatus.PENDING) {
            userMapper.updateStatus(userId, UserStatus.ACTIVE);
        }

        return accountVo;
    }

    /**
     * 부모 회원에게 연결된 ACTIVE 외부 계좌를 조회한다.
     *
     * @param parentId 부모 회원 ID
     * @return 활성 상태의 외부 연동 계좌
     */
    @Override
    public AccountVo getActiveAccount(Long parentId) {
        return accountMapper
                .findActiveByUserId(parentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ACTIVE_PARENT_NOT_FOUND));
    }
}