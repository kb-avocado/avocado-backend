package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.domain.TransactionStatus;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.domain.WalletTransactionType;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.global.config.DataSourceConfig;
import com.avocado.global.config.LocalPropertyConfig;
import com.avocado.global.config.MyBatisConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringJUnitConfig(
        classes = {
                LocalPropertyConfig.class,
                DataSourceConfig.class,
                MyBatisConfig.class
        })
@Transactional
class WalletTxMapperTest {

    @Autowired
    private WalletTxMapper walletTxMapper;

    /**
     * 선불지갑 거래 이력 생성 시 거래 상대방 정보가 정상적으로 저장되는지 확인한다.
     */
    @Test
    @DisplayName("선불지갑 거래 이력 생성 테스트")
    void insertWalletHistory() {
        // given
        WalletHistoryVo history = WalletHistoryVo.builder()
                .walletId(4001L)
                .traceId("TEST-TRACE-001")
                .transactionType(WalletTransactionType.TRANSFER_OUT)
                .amount(10000L)
                .counterpartyWalletId(4002L)
                .counterpartyName("김지호")
                .targetBankCode(null)
                .targetAccountNumber(null)
                .merchantId(null)
                .memo("송금 테스트")
                .status(TransactionStatus.SUCCESS)
                .failureCode(null)
                .build();

        // when
        int result = walletTxMapper.insertWalletHistory(history);

        // then
        assertThat(result).isEqualTo(1);
        assertThat(history.getId()).isNotNull();
    }

}