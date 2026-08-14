package com.avocado.domain.wallet.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private FamilyRelationMapper familyRelationMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("자녀 본인은 자신의 선불지갑을 조회할 수 있다")
    void getChildWallet_childOwner_success() {
        // given
        Long childId = 102L;
        AuthUser authUser = authUser(childId, UserType.CHILD);
        WalletVo wallet = walletVo(childId);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.of(wallet));

        // when
        WalletResponseDto result = walletService.getChildWallet(childId, authUser);

        // then
        assertThat(result.getWalletId()).isEqualTo(2001L);
        assertThat(result.getChildId()).isEqualTo(childId);
        assertThat(result.getWalletNumber()).isEqualTo("WALLET-2026-0001");
        assertThat(result.getBalance()).isEqualTo(48000L);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(familyRelationMapper, never()).existsActiveRelation(authUser.getUserId(), childId);
    }

    @Test
    @DisplayName("연결된 보호자는 자녀의 선불지갑을 조회할 수 있다")
    void getChildWallet_connectedParent_success() {
        // given
        Long parentId = 101L;
        Long childId = 102L;
        AuthUser authUser = authUser(parentId, UserType.PARENT);
        WalletVo wallet = walletVo(childId);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(familyRelationMapper.existsActiveRelation(parentId, childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.of(wallet));

        // when
        WalletResponseDto result = walletService.getChildWallet(childId, authUser);

        // then
        assertThat(result.getWalletId()).isEqualTo(2001L);
        assertThat(result.getChildId()).isEqualTo(childId);
    }

    @Test
    @DisplayName("인증 정보가 없으면 UNAUTHORIZED 예외를 반환한다")
    void getChildWallet_unauthenticated_fail() {
        // when & then
        assertThatThrownBy(() -> walletService.getChildWallet(102L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        verify(userMapper, never()).existsChildById(102L);
    }

    @Test
    @DisplayName("존재하지 않는 자녀이면 CHILD_NOT_FOUND 예외를 반환한다")
    void getChildWallet_childNotFound_fail() {
        // given
        Long childId = 999L;
        AuthUser authUser = authUser(101L, UserType.PARENT);

        when(userMapper.existsChildById(childId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHILD_NOT_FOUND);

        verify(walletMapper, never()).findByChildId(childId);
    }

    @Test
    @DisplayName("다른 가족 자녀의 선불지갑은 조회할 수 없다")
    void getChildWallet_otherFamilyChild_fail() {
        // given
        Long parentId = 101L;
        Long childId = 202L;
        AuthUser authUser = authUser(parentId, UserType.PARENT);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(familyRelationMapper.existsActiveRelation(parentId, childId)).thenReturn(false);

        // when & then
        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(walletMapper, never()).findByChildId(childId);
    }

    @Test
    @DisplayName("자녀가 다른 자녀의 선불지갑을 조회하면 FORBIDDEN 예외를 반환한다")
    void getChildWallet_otherChild_fail() {
        // given
        Long requestedChildId = 103L;
        AuthUser authUser = authUser(102L, UserType.CHILD);

        when(userMapper.existsChildById(requestedChildId)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> walletService.getChildWallet(requestedChildId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(walletMapper, never()).findByChildId(requestedChildId);
    }

    @Test
    @DisplayName("자녀는 존재하지만 선불지갑이 없으면 WALLET_NOT_FOUND 예외를 반환한다")
    void getChildWallet_walletNotFound_fail() {
        // given
        Long childId = 102L;
        AuthUser authUser = authUser(childId, UserType.CHILD);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WALLET_NOT_FOUND);
    }

    private AuthUser authUser(
            Long userId,
            UserType userType
    ) {
        return AuthUser.builder()
                .userId(userId)
                .role(UserRole.USER)
                .userType(userType)
                .build();
    }

    private WalletVo walletVo(Long childId) {
        WalletVo wallet = new WalletVo();
        wallet.setId(2001L);
        wallet.setChildId(childId);
        wallet.setWalletNumber("WALLET-2026-0001");
        wallet.setBalance(48000L);
        wallet.setStatus("ACTIVE");
        return wallet;
    }
}
