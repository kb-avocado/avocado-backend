package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringJUnitConfig(classes = RootConfig.class)
class WalletTxMapperTest {

    @Autowired
    private WalletTxMapper walletTxMapper;

    @Test
    @DisplayName("선불지갑의 전체 거래 건수를 조회한다")
    void countByWalletId() {
        // given
        Long walletId = 2001L;

        // when
        long count = walletTxMapper.countByWalletId(walletId);

        // then
        assertThat(count).isGreaterThan(0);
    }

    @Test
    @DisplayName("선불지갑의 거래 목록을 조회한다")
    void findAllByWalletId() {
        // given
        Long walletId = 2001L;
        int offset = 0;
        int size = 20;

        // when
        List<WalletTxItemResponseDto> result = walletTxMapper.findAllByWalletId(
                walletId,
                offset,
                size
        );

        // then
        assertThat(result)
                .as("거래 목록 조회 결과는 null이 아니어야 한다")
                .isNotNull();

        assertThat(result)
                .as("테스트용 선불지갑에는 거래 내역이 존재해야 한다")
                .isNotEmpty();
    }

    @Test
    @DisplayName("선불지갑 ID와 거래 ID로 거래 상세 정보를 조회한다")
    void findDetailByWalletIdAndTransactionId() {
        // given
        Long walletId = 2001L;
        Long transactionId = 20007L;

        // when
        Optional<WalletTxDetailResponseDto> detail = walletTxMapper.findDetailByWalletIdAndTransactionId(
                walletId,
                transactionId
        );

        // then
        assertThat(detail).isPresent();

        WalletTxDetailResponseDto response = detail.get();

        assertThat(response.getTransactionId()).isEqualTo(transactionId);
        assertThat(response.getTransactionType()).isNotBlank();
        assertThat(response.getAmount()).isPositive();
        assertThat(response.getStatus()).isNotBlank();
        assertThat(response.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("다른 선불지갑의 거래는 조회할 수 없다")
    void findDetailByWalletIdAndTransactionIdWhenWalletDoesNotMatch() {
        // given
        Long walletId = 2001L;

        // 2001번 지갑에 속하지 않는 거래 ID
        Long transactionId = 20010L;

        // when
        Optional<WalletTxDetailResponseDto> detail = walletTxMapper.findDetailByWalletIdAndTransactionId(
                walletId,
                transactionId
        );

        // then
        assertThat(detail).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 거래 ID를 조회하면 빈 값을 반환한다")
    void findDetailByWalletIdAndTransactionIdWhenTransactionDoesNotExist() {
        // given
        Long walletId = 2001L;
        Long transactionId = Long.MAX_VALUE;

        // when
        Optional<WalletTxDetailResponseDto> detail = walletTxMapper.findDetailByWalletIdAndTransactionId(
                walletId,
                transactionId
        );

        // then
        assertThat(detail).isEmpty();
    }
}