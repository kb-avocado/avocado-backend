package com.avocado.piggybank.service;

import com.avocado.piggybank.domain.PiggyBank;
import com.avocado.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.piggybank.mapper.PiggyBankMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PiggyBankServiceImplTest {

    @Mock
    private PiggyBankMapper piggyBankMapper;

    @InjectMocks
    private PiggyBankServiceImpl piggyBankService;

    @Test
    @DisplayName("진행중 목록 조회 - 진행률 계산과 응답 구조 확인")
    void getList_inProgress() {
        // given (가짜 데이터 준비)
        PiggyBank p = PiggyBank.builder()
                .id(1L).walletId(1L).name("레고 우주선")
                .targetAmount(180000L).balance(135000L)
                .status("ACTIVE").isFavorite(true)
                .build();

        List<String> inProgress = List.of("ACTIVE", "PENDING_ACHIEVE");
        when(piggyBankMapper.selectByWalletIdAndStatuses(1L, inProgress)).thenReturn(List.of(p));
        when(piggyBankMapper.countByWalletIdAndStatuses(1L, inProgress)).thenReturn(1);

        // when (실제 실행)
        PiggyBankListResponseDto result = piggyBankService.getList(1L, "IN_PROGRESS");

        // then (검증)
        assertThat(result.getPiggyBanks()).hasSize(1);
        assertThat(result.getPiggyBanks().get(0).getProgressRate()).isEqualTo(75);
        assertThat(result.getPiggyBanks().get(0).getSavedAmount()).isEqualTo(135000L);
        assertThat(result.getActiveCount()).isEqualTo(1);
        assertThat(result.isCanCreate()).isTrue();
    }
    @Test
    @DisplayName("완료 탭 - ACHIEVE/CANCEL 상태로 조회")
    void getList_closed() {
        when(piggyBankMapper.selectByWalletIdAndStatuses(1L, List.of("ACHIEVE", "CANCEL")))
                .thenReturn(List.of());
        when(piggyBankMapper.countByWalletIdAndStatuses(1L, List.of("ACTIVE", "PENDING_ACHIEVE")))
                .thenReturn(0);

        PiggyBankListResponseDto result = piggyBankService.getList(1L, "CLOSED");

        assertThat(result.getPiggyBanks()).isEmpty();
    }
    @Test
    @DisplayName("진행중 3개면 더 못 만든다")
    void getList_maxReached() {
        List<String> inProgress = List.of("ACTIVE", "PENDING_ACHIEVE");
        when(piggyBankMapper.selectByWalletIdAndStatuses(1L, inProgress)).thenReturn(List.of());
        when(piggyBankMapper.countByWalletIdAndStatuses(1L, inProgress)).thenReturn(3);

        PiggyBankListResponseDto result = piggyBankService.getList(1L, "IN_PROGRESS");

        assertThat(result.getActiveCount()).isEqualTo(3);
        assertThat(result.isCanCreate()).isFalse();
    }
}