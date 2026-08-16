package com.avocado.domain.wallet.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletStatus;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private FamilyRelationMapper familyRelationMapper;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private WalletTxMapper walletTxMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    /**
     * 선불지갑이 없는 아이에게 초기 상태의 선불지갑을 발급하는지 확인한다.
     */
    @Test
    @DisplayName("아이 선불지갑 발급 성공")
    void issueWalletSuccess() {
        // given
        Long childId = 102L;

        given(walletMapper.existsByChildId(childId))
                .willReturn(false);

        // 생성되는 임의의 계좌번호는 기존에 존재하지 않는 것으로 처리한다.
        given(walletMapper.existsByWalletNumber(anyString()))
                .willReturn(false);

        given(walletMapper.insert(any(WalletVo.class)))
                .willReturn(1);

        // when
        walletService.issueWallet(childId);

        // then
        ArgumentCaptor<WalletVo> captor =
                ArgumentCaptor.forClass(WalletVo.class);

        verify(walletMapper).insert(captor.capture());

        WalletVo wallet = captor.getValue();

        assertThat(wallet.getChildId()).isEqualTo(childId);
        assertThat(wallet.getBalance()).isEqualTo(0L);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);

        // 계좌번호가 12자리 숫자로 생성되었는지 확인한다.
        assertThat(wallet.getWalletNumber())
                .matches("\\d{12}");

        // 첫 번째 자리가 0이 아닌지 확인한다.
        assertThat(wallet.getWalletNumber())
                .doesNotStartWith("0");
    }

    /**
     * 이미 선불지갑을 보유한 아이에게는 추가 지갑을 발급하지 않는지 확인한다.
     */
    @Test
    @DisplayName("이미 선불지갑이 존재하면 발급 실패")
    void issueWalletAlreadyExists() {
        // given
        Long childId = 102L;

        given(walletMapper.existsByChildId(childId))
                .willReturn(true);

        // when & then
        assertThatThrownBy(
                () -> walletService.issueWallet(childId)
        )
                .isInstanceOf(BusinessException.class);

        // 기존 지갑이 있으면 계좌번호 생성 및 지갑 저장까지 진행하지 않는다.
        verify(walletMapper, never())
                .existsByWalletNumber(anyString());

        verify(walletMapper, never())
                .insert(any(WalletVo.class));
    }

    /**
     * 지갑 저장에 실패하면 예외가 발생하는지 확인한다.
     */
    @Test
    @DisplayName("선불지갑 저장 실패")
    void issueWalletInsertFail() {
        // given
        Long childId = 102L;

        given(walletMapper.existsByChildId(childId))
                .willReturn(false);

        given(walletMapper.existsByWalletNumber(anyString()))
                .willReturn(false);

        given(walletMapper.insert(any(WalletVo.class)))
                .willReturn(0);

        // when & then
        assertThatThrownBy(
                () -> walletService.issueWallet(childId)
        )
                .isInstanceOf(BusinessException.class);
    }
}