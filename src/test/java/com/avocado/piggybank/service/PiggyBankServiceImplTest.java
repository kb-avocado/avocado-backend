package com.avocado.piggybank.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.piggybank.domain.PiggyBank;
import com.avocado.piggybank.dto.response.PiggyBankListResponseDto;
import com.avocado.piggybank.mapper.PiggyBankMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.avocado.piggybank.domain.BonusType;
import com.avocado.piggybank.dto.response.PiggyBankDetailResponseDto;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;
import com.avocado.piggybank.dto.request.PiggyBankCreateRequestDto;
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
    @Test
    @DisplayName("상세 조회 - 남은 금액과 진행률 계산")
    void getDetail_success() {
        // given
        PiggyBank p = PiggyBank.builder()
                .id(1L).walletId(1L).name("레고 우주선")
                .targetAmount(180000L).balance(135000L)
                .status("ACTIVE").isFavorite(true)
                .bonusType(BonusType.FIXED).bonusValue(10000L)
                .build();
        when(piggyBankMapper.selectById(1L)).thenReturn(p);

        // when
        PiggyBankDetailResponseDto result = piggyBankService.getDetail(1L);

        // then
        assertThat(result.getPiggyBankId()).isEqualTo(1L);
        assertThat(result.getProgressRate()).isEqualTo(75);          // 135000/180000
        assertThat(result.getRemainingAmount()).isEqualTo(45000L);   // 180000-135000
        assertThat(result.getBonusType()).isEqualTo("FIXED");
        assertThat(result.getBonusValue()).isEqualTo(10000L);
    }

    @Test
    @DisplayName("상세 조회 - 없는 저금통이면 BusinessException(PIGGY_BANK_NOT_FOUND)")
    void getDetail_notFound() {
        // given
        when(piggyBankMapper.selectById(999L)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> piggyBankService.getDetail(999L))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PIGGY_BANK_NOT_FOUND);
    }
    @Test
    @DisplayName("상세 조회 - 목표 달성 시 진행률 100, 남은 금액 0")
    void getDetail_achieved() {
        PiggyBank p = PiggyBank.builder()
                .id(5L).name("여행 가방").targetAmount(200000L).balance(200000L)
                .status("ACHIEVE").isFavorite(false)
                .bonusType(BonusType.NONE).bonusValue(0L)
                .build();
        when(piggyBankMapper.selectById(5L)).thenReturn(p);

        PiggyBankDetailResponseDto result = piggyBankService.getDetail(5L);

        assertThat(result.getProgressRate()).isEqualTo(100);
        assertThat(result.getRemainingAmount()).isEqualTo(0L);
        assertThat(result.getBonusType()).isEqualTo("NONE");
    }

    @Test
    @DisplayName("상세 조회 - 목표 금액 0이면 진행률 0 (0으로 나누기 방지)")
    void getDetail_zeroTarget() {
        PiggyBank p = PiggyBank.builder()
                .id(6L).name("테스트").targetAmount(0L).balance(0L)
                .status("ACTIVE").isFavorite(false)
                .bonusType(BonusType.NONE).bonusValue(0L)
                .build();
        when(piggyBankMapper.selectById(6L)).thenReturn(p);

        PiggyBankDetailResponseDto result = piggyBankService.getDetail(6L);

        assertThat(result.getProgressRate()).isEqualTo(0);  // 0/0 → 0 처리
    }

    @Test
    @DisplayName("생성 - 정상 생성")
    void create_success() {
        PiggyBankCreateRequestDto req = new PiggyBankCreateRequestDto();
        req.setName("게임기");
        req.setTargetAmount(120000L);

        List<String> inProgress = List.of("ACTIVE", "PENDING_ACHIEVE");
        when(piggyBankMapper.countByWalletIdAndStatuses(1L, inProgress)).thenReturn(2);
        when(piggyBankMapper.selectLastInsertId()).thenReturn(10L);
        when(piggyBankMapper.selectById(10L)).thenReturn(
                PiggyBank.builder().id(10L).walletId(1L).name("게임기")
                        .targetAmount(120000L).balance(0L).status("ACTIVE")
                        .isFavorite(false).bonusType(BonusType.NONE).bonusValue(0L).build());

        PiggyBankDetailResponseDto result = piggyBankService.create(1L, req);

        assertThat(result.getPiggyBankId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("게임기");
        verify(piggyBankMapper).insert(any(PiggyBank.class));
    }

    @Test
    @DisplayName("생성 - 3개 초과면 예외")
    void create_limitExceeded() {
        PiggyBankCreateRequestDto req = new PiggyBankCreateRequestDto();
        req.setName("초과");
        req.setTargetAmount(10000L);
        when(piggyBankMapper.countByWalletIdAndStatuses(1L, List.of("ACTIVE", "PENDING_ACHIEVE")))
                .thenReturn(3);

        assertThatThrownBy(() -> piggyBankService.create(1L, req))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.PIGGY_BANK_LIMIT_EXCEEDED);
    }
}