package com.avocado.domain.transfer.mapper;

import com.avocado.domain.transfer.domain.TransferRecipientVo;
import com.avocado.global.config.RootConfig;
import com.avocado.global.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class,
        SecurityConfig.class
})
@Transactional
class TransferRecipientMapperTest {

    @Autowired
    private TransferRecipientMapper transferRecipientMapper;

    /**
     * 아이의 최근 송금 수취처 전체 개수를 조회한다.
     */
    @Test
    @DisplayName("최근 송금 수취처 개수 조회")
    void countRecentByChildId() {
        // given
        // wallet_id = 2001을 소유한 아이 ID로 변경한다.
        Long childId = 102L;

        // when
        long count =
                transferRecipientMapper.countRecentByChildId(
                        childId
                );

        // then
        // wallet 2001은 김지호와 박서준 두 수취처에 성공적으로 송금했다.
        assertThat(count)
                .isEqualTo(2L);
    }

    /**
     * 아이의 내부 지갑 및 외부 계좌 최근 수취처를 최신순으로 조회한다.
     */
    @Test
    @DisplayName("최근 송금 수취처 목록 조회")
    void findRecentByChildId() {
        // given
        // wallet_id = 2001을 소유한 아이 ID로 변경한다.
        Long childId = 102L;

        int offset = 0;
        int size = 10;

        // when
        List<TransferRecipientVo> recipients =
                transferRecipientMapper.findRecentByChildId(
                        childId,
                        offset,
                        size
                );

        // then
        assertThat(recipients)
                .hasSize(2);

        // 가장 최근 송금인 우리은행 박서준이 첫 번째로 조회되어야 한다.
        TransferRecipientVo firstRecipient =
                recipients.get(0);

        assertThat(firstRecipient.getRecipientName())
                .isEqualTo("박서준");

        assertThat(firstRecipient.getBankCode())
                .isEqualTo("020");

        assertThat(firstRecipient.getRecipientNumber())
                .isEqualTo("10023456789012");

        assertThat(firstRecipient.getTransferredAt())
                .isEqualTo(
                        LocalDateTime.of(
                                2026,
                                7,
                                11,
                                16,
                                0,
                                0
                        )
                );

        // 그 다음 송금인 아보카도 내부 지갑 김지호가 조회되어야 한다.
        TransferRecipientVo secondRecipient =
                recipients.get(1);

        assertThat(secondRecipient.getRecipientName())
                .isEqualTo("김지호");

        assertThat(secondRecipient.getBankCode())
                .isEqualTo("999");

        // 내부 지갑 번호는 wallets.id = 2002의 wallet_number가 반환되어야 한다.
        assertThat(secondRecipient.getRecipientNumber())
                .isNotBlank();

        assertThat(secondRecipient.getTransferredAt())
                .isEqualTo(
                        LocalDateTime.of(
                                2026,
                                7,
                                6,
                                14,
                                0,
                                0
                        )
                );
    }

    /**
     * 최근 송금 수취처에 offset과 size가 정상 적용되는지 확인한다.
     */
    @Test
    @DisplayName("최근 송금 수취처 페이지네이션")
    void findRecentByChildIdPagination() {
        // given
        // wallet_id = 2001을 소유한 아이 ID로 변경한다.
        Long childId = 102L;

        // when
        List<TransferRecipientVo> firstPage =
                transferRecipientMapper.findRecentByChildId(
                        childId,
                        0,
                        1
                );

        List<TransferRecipientVo> secondPage =
                transferRecipientMapper.findRecentByChildId(
                        childId,
                        1,
                        1
                );

        // then
        assertThat(firstPage)
                .hasSize(1);

        assertThat(secondPage)
                .hasSize(1);

        // 첫 번째 페이지에는 가장 최근 수취처인 박서준이 조회되어야 한다.
        assertThat(firstPage.get(0).getRecipientName())
                .isEqualTo("박서준");

        // 두 번째 페이지에는 그 다음 수취처인 김지호가 조회되어야 한다.
        assertThat(secondPage.get(0).getRecipientName())
                .isEqualTo("김지호");
    }
}