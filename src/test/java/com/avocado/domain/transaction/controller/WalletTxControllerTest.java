package com.avocado.domain.transaction.controller;

import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.domain.transaction.service.WalletTxService;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserType;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.PageResponse;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalletTxControllerTest {

    @Mock
    private WalletTxService walletTxService;

    private WalletTxController walletTxController;

    @BeforeEach
    void setUp() {
        walletTxController = new WalletTxController(walletTxService);
    }

    @Test
    @DisplayName("지갑 거래 목록 조회는 인증 사용자 ID를 서비스에 전달한다")
    void getWalletTxListUsesAuthenticatedUserId() {
        // given
        AuthUser authUser = authUser(205L);
        WalletTxListRequestDto request = new WalletTxListRequestDto();
        PageResponse<WalletTxItemResponseDto> pageResponse = PageResponse.of(
                0,
                20,
                0,
                List.of()
        );

        when(walletTxService.getWalletTxList(authUser.getUserId(), request))
                .thenReturn(pageResponse);

        // when
        ResponseEntity<ApiResponse<PageResponse<WalletTxItemResponseDto>>> result =
                walletTxController.getWalletTxList(authUser, request);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(pageResponse);
        verify(walletTxService).getWalletTxList(205L, request);
    }

    @Test
    @DisplayName("지갑 거래 상세 조회는 인증 사용자 ID를 서비스에 전달한다")
    void getWalletTxDetailUsesAuthenticatedUserId() {
        // given
        AuthUser authUser = authUser(205L);
        Long transactionId = 9001L;
        WalletTxDetailResponseDto detailResponse = WalletTxDetailResponseDto.builder()
                .transactionId(transactionId)
                .build();

        when(walletTxService.getWalletTxDetail(authUser.getUserId(), transactionId))
                .thenReturn(detailResponse);

        // when
        ResponseEntity<ApiResponse<WalletTxDetailResponseDto>> result =
                walletTxController.getWalletTxDetail(authUser, transactionId);

        // then
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getData()).isSameAs(detailResponse);
        verify(walletTxService).getWalletTxDetail(205L, transactionId);
    }

    @Test
    @DisplayName("인증 정보가 없으면 UNAUTHORIZED 예외를 반환한다")
    void getWalletTxListWithoutAuthUserThrowsUnauthorized() {
        // given
        WalletTxListRequestDto request = new WalletTxListRequestDto();

        // when & then
        assertThatThrownBy(() -> walletTxController.getWalletTxList(null, request))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);
    }

    private AuthUser authUser(Long userId) {
        return AuthUser.builder()
                .userId(userId)
                .role(UserRole.USER)
                .userType(UserType.CHILD)
                .build();
    }
}
