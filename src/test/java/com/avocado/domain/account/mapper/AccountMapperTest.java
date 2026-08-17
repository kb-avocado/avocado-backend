package com.avocado.domain.account.mapper;

import com.avocado.domain.account.domain.AccountStatus;
import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.mapper.AccountMapper;
import com.avocado.global.config.DataSourceConfig;
import com.avocado.global.config.LocalPropertyConfig;
import com.avocado.global.config.MyBatisConfig;
import com.avocado.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(classes = {
        LocalPropertyConfig.class,
        DataSourceConfig.class,
        MyBatisConfig.class
})
@Transactional
class AccountMapperTest {

    @Autowired
    private AccountMapper accountMapper;

    @Test
    @DisplayName("은행 코드와 계좌 번호가 일치하는 계좌가 존재하면 true를 반환")
    void existsByBankCodeAndAccountNumber_returnTrue() {
        // given
        String bankCode = "004";
        String accountNumber = "11012345678901";

        // when
        boolean exists = accountMapper.existsByBankCodeAndAccountNumber(
                bankCode,
                accountNumber
        );

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("은행 코드와 계좌 번호가 일치하는 계좌가 존재하지 않으면 false를 반환")
    void existsByBankCodeAndAccountNumber_returnFalse() {
        // given
        String bankCode = "999";
        String accountNumber = "11012345678901";

        // when
        boolean exists = accountMapper.existsByBankCodeAndAccountNumber(
                bankCode,
                accountNumber
        );

        // then
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("유효한 계좌 정보를 등록할 경우 계좌가 한 건 생성되고 PK가 채워진다")
    void insertAccount_returnsOne() {
        // given
        AccountVo accountVo = AccountVo.builder()
                .userId(101L)
                .bankCode("004")
                .accountNumber("11098765432199")
                .status("ACTIVE")
                .build();

        // when
        int insertedCount = accountMapper.insertAccount(accountVo);

        // then
        assertThat(insertedCount).isEqualTo(1);
        assertThat(accountVo.getId()).isNotNull();
    }

    @Test
    @DisplayName("이미 등록된 은행 코드와 계좌 번호를 저장하면 예외가 발생한다")
    void insertAccount_throwsException() {
        // given
        AccountVo accountVo = AccountVo.builder()
                .userId(101L)
                .bankCode("004")
                .accountNumber("11012345678901")
                .status(AccountStatus.ACTIVE.getDescription())
                .build();

        // when, then
        assertThatThrownBy(() ->
                accountMapper.insertAccount(accountVo)
        ).isInstanceOf(DuplicateKeyException.class);
    }
}