package com.avocado.domain.transaction.mapper;

import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

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
}