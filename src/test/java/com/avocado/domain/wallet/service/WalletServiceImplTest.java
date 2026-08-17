package com.avocado.domain.wallet.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.merchant.domain.MerchantVo;
import com.avocado.domain.merchant.service.MerchantService;
import com.avocado.domain.payment.domain.PaymentRequestedResult;
import com.avocado.domain.payment.domain.PaymentSimulationResult;
import com.avocado.domain.transaction.domain.TransactionStatus;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.domain.WalletTransactionType;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.domain.WalletStatus;
import com.avocado.domain.wallet.domain.WalletVo;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @Mock
    private MerchantService merchantService;

    @InjectMocks
    private WalletServiceImpl walletService;

    @Test
    @DisplayName("아이 선불지갑 발급 성공")
    void issueWalletSuccess() {
        Long childId = 102L;

        when(walletMapper.existsByChildId(childId)).thenReturn(false);
        when(walletMapper.existsByWalletNumber(anyString())).thenReturn(false);
        when(walletMapper.insert(any(WalletVo.class))).thenReturn(1);

        walletService.issueWallet(childId);

        ArgumentCaptor<WalletVo> captor = ArgumentCaptor.forClass(WalletVo.class);
        verify(walletMapper).insert(captor.capture());

        WalletVo wallet = captor.getValue();
        assertThat(wallet.getChildId()).isEqualTo(childId);
        assertThat(wallet.getBalance()).isEqualTo(0L);
        assertThat(wallet.getStatus()).isEqualTo(WalletStatus.ACTIVE);
        assertThat(wallet.getWalletNumber()).matches("\\d{12}");
        assertThat(wallet.getWalletNumber()).doesNotStartWith("0");
    }

    @Test
    @DisplayName("이미 선불지갑이 존재하면 발급 실패")
    void issueWalletAlreadyExists() {
        Long childId = 102L;

        when(walletMapper.existsByChildId(childId)).thenReturn(true);

        assertThatThrownBy(() -> walletService.issueWallet(childId))
                .isInstanceOf(BusinessException.class);

        verify(walletMapper, never()).existsByWalletNumber(anyString());
        verify(walletMapper, never()).insert(any(WalletVo.class));
    }

    @Test
    @DisplayName("선불지갑 저장 실패")
    void issueWalletInsertFail() {
        Long childId = 102L;

        when(walletMapper.existsByChildId(childId)).thenReturn(false);
        when(walletMapper.existsByWalletNumber(anyString())).thenReturn(false);
        when(walletMapper.insert(any(WalletVo.class))).thenReturn(0);

        assertThatThrownBy(() -> walletService.issueWallet(childId))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("자녀 본인은 자신의 선불지갑을 조회할 수 있다")
    void getChildWallet_childOwner_success() {
        Long childId = 102L;
        AuthUser authUser = authUser(childId, UserType.CHILD);
        WalletVo wallet = walletVo(childId, 48000L, WalletStatus.ACTIVE);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.of(wallet));

        WalletResponseDto result = walletService.getChildWallet(childId, authUser);

        assertThat(result.getWalletId()).isEqualTo(2001L);
        assertThat(result.getChildId()).isEqualTo(childId);
        assertThat(result.getWalletNumber()).isEqualTo("123456789012");
        assertThat(result.getBalance()).isEqualTo(48000L);
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        verify(familyRelationMapper, never()).existsActiveRelation(authUser.getUserId(), childId);
    }

    @Test
    @DisplayName("연결된 보호자는 자녀의 선불지갑을 조회할 수 있다")
    void getChildWallet_connectedParent_success() {
        Long parentId = 101L;
        Long childId = 102L;
        AuthUser authUser = authUser(parentId, UserType.PARENT);
        WalletVo wallet = walletVo(childId, 48000L, WalletStatus.ACTIVE);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(familyRelationMapper.existsActiveRelation(parentId, childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.of(wallet));

        WalletResponseDto result = walletService.getChildWallet(childId, authUser);

        assertThat(result.getWalletId()).isEqualTo(2001L);
        assertThat(result.getChildId()).isEqualTo(childId);
    }

    @Test
    @DisplayName("인증 정보가 없으면 UNAUTHORIZED 예외를 반환한다")
    void getChildWallet_unauthenticated_fail() {
        assertThatThrownBy(() -> walletService.getChildWallet(102L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.UNAUTHORIZED);

        verify(userMapper, never()).existsChildById(102L);
    }

    @Test
    @DisplayName("존재하지 않는 자녀이면 CHILD_NOT_FOUND 예외를 반환한다")
    void getChildWallet_childNotFound_fail() {
        Long childId = 999L;
        AuthUser authUser = authUser(101L, UserType.PARENT);

        when(userMapper.existsChildById(childId)).thenReturn(false);

        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.CHILD_NOT_FOUND);

        verify(walletMapper, never()).findByChildId(childId);
    }

    @Test
    @DisplayName("다른 가족 자녀의 선불지갑은 조회할 수 없다")
    void getChildWallet_otherFamilyChild_fail() {
        Long parentId = 101L;
        Long childId = 202L;
        AuthUser authUser = authUser(parentId, UserType.PARENT);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(familyRelationMapper.existsActiveRelation(parentId, childId)).thenReturn(false);

        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(walletMapper, never()).findByChildId(childId);
    }

    @Test
    @DisplayName("자녀가 다른 자녀의 선불지갑을 조회하면 FORBIDDEN 예외를 반환한다")
    void getChildWallet_otherChild_fail() {
        Long requestedChildId = 103L;
        AuthUser authUser = authUser(102L, UserType.CHILD);

        when(userMapper.existsChildById(requestedChildId)).thenReturn(true);

        assertThatThrownBy(() -> walletService.getChildWallet(requestedChildId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.FORBIDDEN);

        verify(walletMapper, never()).findByChildId(requestedChildId);
    }

    @Test
    @DisplayName("자녀는 존재하지만 선불지갑이 없으면 WALLET_NOT_FOUND 예외를 반환한다")
    void getChildWallet_walletNotFound_fail() {
        Long childId = 102L;
        AuthUser authUser = authUser(childId, UserType.CHILD);

        when(userMapper.existsChildById(childId)).thenReturn(true);
        when(walletMapper.findByChildId(childId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getChildWallet(childId, authUser))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.WALLET_NOT_FOUND);
    }

    @Test
    @DisplayName("POS 결제 성공 시 잔액을 차감하고 성공 이력과 OUT 원장을 저장한다")
    void processPosPayment_success() {
        WalletVo wallet = walletVo(102L, 48000L, WalletStatus.ACTIVE);
        MerchantVo merchant = merchantVo(3001L, "아보카도 편의점", false, "ACTIVE");
        ArgumentCaptor<WalletHistoryVo> historyCaptor = ArgumentCaptor.forClass(WalletHistoryVo.class);

        when(walletMapper.findForUpdateByChildId(102L)).thenReturn(Optional.of(wallet));
        when(merchantService.findById(3001L)).thenReturn(Optional.of(merchant));
        when(walletMapper.decreaseBalance(2001L, 12000L)).thenReturn(1);
        when(walletTxMapper.insertWalletHistory(any(WalletHistoryVo.class))).thenAnswer(invocation -> {
            WalletHistoryVo history = invocation.getArgument(0);
            ReflectionTestUtils.setField(history, "id", 9001L);
            return 1;
        });
        when(walletTxMapper.insertWalletLedger(9001L, 2001L, "OUT", 12000L, 48000L, 36000L))
                .thenReturn(1);

        PaymentSimulationResult result = walletService.processPosPayment(
                102L,
                3001L,
                12000L,
                PaymentRequestedResult.SUCCESS
        );

        assertThat(result.getWalletHistoryId()).isEqualTo(9001L);
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getBalanceAfter()).isEqualTo(36000L);

        verify(walletTxMapper).insertWalletHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getTransactionType()).isEqualTo(WalletTransactionType.PAYMENT);
        assertThat(historyCaptor.getValue().getMerchantId()).isEqualTo(3001L);
        assertThat(historyCaptor.getValue().getStatus()).isEqualTo(TransactionStatus.SUCCESS);
        assertThat(historyCaptor.getValue().getFailureCode()).isNull();
    }

    @Test
    @DisplayName("POS 임의 실패는 잔액 차감 없이 실패 이력만 저장한다")
    void processPosPayment_forceFail() {
        WalletVo wallet = walletVo(102L, 48000L, WalletStatus.ACTIVE);
        MerchantVo merchant = merchantVo(3001L, "아보카도 편의점", false, "ACTIVE");
        ArgumentCaptor<WalletHistoryVo> historyCaptor = ArgumentCaptor.forClass(WalletHistoryVo.class);

        when(walletMapper.findForUpdateByChildId(102L)).thenReturn(Optional.of(wallet));
        when(merchantService.findById(3001L)).thenReturn(Optional.of(merchant));
        when(walletTxMapper.insertWalletHistory(any(WalletHistoryVo.class))).thenAnswer(invocation -> {
            WalletHistoryVo history = invocation.getArgument(0);
            ReflectionTestUtils.setField(history, "id", 9002L);
            return 1;
        });

        PaymentSimulationResult result = walletService.processPosPayment(
                102L,
                3001L,
                12000L,
                PaymentRequestedResult.FORCE_FAIL
        );

        assertThat(result.getWalletHistoryId()).isEqualTo(9002L);
        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureCode()).isEqualTo(ErrorCode.FORCED_FAILURE);
        assertThat(result.getBalanceAfter()).isEqualTo(48000L);

        verify(walletMapper, never()).decreaseBalance(2001L, 12000L);
        verify(walletTxMapper, never()).insertWalletLedger(anyLong(), anyLong(), anyString(), anyLong(), anyLong(), anyLong());
        verify(walletTxMapper).insertWalletHistory(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getStatus()).isEqualTo(TransactionStatus.FAILED);
        assertThat(historyCaptor.getValue().getFailureCode()).isEqualTo("FORCED_FAILURE");
    }

    @Test
    @DisplayName("잔액 부족은 잔액 차감 없이 실패 이력으로 저장한다")
    void processPosPayment_insufficientBalance() {
        WalletVo wallet = walletVo(102L, 8000L, WalletStatus.ACTIVE);
        MerchantVo merchant = merchantVo(3001L, "아보카도 편의점", false, "ACTIVE");

        when(walletMapper.findForUpdateByChildId(102L)).thenReturn(Optional.of(wallet));
        when(merchantService.findById(3001L)).thenReturn(Optional.of(merchant));
        when(walletTxMapper.insertWalletHistory(any(WalletHistoryVo.class))).thenAnswer(invocation -> {
            WalletHistoryVo history = invocation.getArgument(0);
            ReflectionTestUtils.setField(history, "id", 9003L);
            return 1;
        });

        PaymentSimulationResult result = walletService.processPosPayment(
                102L,
                3001L,
                12000L,
                PaymentRequestedResult.SUCCESS
        );

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureCode()).isEqualTo(ErrorCode.INSUFFICIENT_BALANCE);
        assertThat(result.getBalanceAfter()).isEqualTo(8000L);
        verify(walletMapper, never()).decreaseBalance(2001L, 12000L);
        verify(walletTxMapper, never()).insertWalletLedger(anyLong(), anyLong(), anyString(), anyLong(), anyLong(), anyLong());
    }

    @Test
    @DisplayName("아이 제한 가맹점은 잔액 차감 없이 실패 이력으로 저장한다")
    void processPosPayment_restrictedMerchant() {
        WalletVo wallet = walletVo(102L, 48000L, WalletStatus.ACTIVE);
        MerchantVo merchant = merchantVo(3003L, "성인 주류마켓", true, "ACTIVE");

        when(walletMapper.findForUpdateByChildId(102L)).thenReturn(Optional.of(wallet));
        when(merchantService.findById(3003L)).thenReturn(Optional.of(merchant));
        when(walletTxMapper.insertWalletHistory(any(WalletHistoryVo.class))).thenAnswer(invocation -> {
            WalletHistoryVo history = invocation.getArgument(0);
            ReflectionTestUtils.setField(history, "id", 9004L);
            return 1;
        });

        PaymentSimulationResult result = walletService.processPosPayment(
                102L,
                3003L,
                12000L,
                PaymentRequestedResult.SUCCESS
        );

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getFailureCode()).isEqualTo(ErrorCode.MERCHANT_RESTRICTED);
        assertThat(result.getBalanceAfter()).isEqualTo(48000L);
        verify(walletMapper, never()).decreaseBalance(2001L, 12000L);
        verify(walletTxMapper, never()).insertWalletLedger(anyLong(), anyLong(), anyString(), anyLong(), anyLong(), anyLong());
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

    private WalletVo walletVo(
            Long childId,
            Long balance,
            WalletStatus status
    ) {
        return WalletVo.builder()
                .id(2001L)
                .childId(childId)
                .walletNumber("123456789012")
                .balance(balance)
                .status(status)
                .build();
    }

    private MerchantVo merchantVo(
            Long merchantId,
            String name,
            Boolean restrictedForChild,
            String status
    ) {
        MerchantVo merchant = new MerchantVo();
        merchant.setId(merchantId);
        merchant.setName(name);
        merchant.setRestrictedForChild(restrictedForChild);
        merchant.setStatus(status);
        return merchant;
    }
}
