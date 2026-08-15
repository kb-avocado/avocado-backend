package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.RefreshResult;
import com.avocado.domain.user.domain.UserVo;
import com.avocado.domain.user.domain.UserRole;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.user.repository.RefreshTokenRepository;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 리프레시 토큰 재발급(회전)과 재사용 감지를 검증한다.
 *
 * Redis 접근은 RefreshTokenRepository 뒤에 있으므로 Mock으로 대신하고,
 * 여기서는 "정상 / 탈취 / 만료·위조" 세 갈래 판정만 본다.
 */
@ExtendWith(MockitoExtension.class)
class UserLoginServiceImplTest {

    private static final Long USER_ID = 203L;
    private static final String OLD_TOKEN = "old-refresh-token";
    private static final String NEW_TOKEN = "new-refresh-token";

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    private UserLoginServiceImpl userLoginService;

    @BeforeEach
    void setUp() {
        userLoginService = new UserLoginServiceImpl(
                userMapper,
                passwordEncoder,
                refreshTokenRepository
        );
    }

    @Test
    @DisplayName("유효한 리프레시 토큰이면 회전시키고 새 토큰을 돌려준다")
    void refresh_success() {
        // given
        when(refreshTokenRepository.findUserId(OLD_TOKEN)).thenReturn(Optional.of(USER_ID));
        when(userMapper.findById(USER_ID)).thenReturn(Optional.of(parent(UserStatus.ACTIVE)));
        when(refreshTokenRepository.rotate(USER_ID, OLD_TOKEN)).thenReturn(NEW_TOKEN);

        // when
        RefreshResult result = userLoginService.refresh(OLD_TOKEN);

        // then
        assertThat(result.getRefreshToken()).isEqualTo(NEW_TOKEN);
        assertThat(result.getUser().getUserId()).isEqualTo(USER_ID);
        assertThat(result.getUser().getType()).isEqualTo(UserType.PARENT);

        // 정상 흐름에서는 폐기가 일어나선 안 된다.
        verify(refreshTokenRepository, never()).revokeAll(anyLong());
    }

    @Test
    @DisplayName("이미 회전된 토큰이 다시 오면 탈취로 보고 그 회원의 모든 세션을 끊는다")
    void refresh_reusedToken_revokesEverySession() {
        // given: token에는 없지만 used에 남아 있다 = 한때 유효했던 토큰이 다시 왔다
        when(refreshTokenRepository.findUserId(OLD_TOKEN)).thenReturn(Optional.empty());
        when(refreshTokenRepository.findUsedUserId(OLD_TOKEN)).thenReturn(Optional.of(USER_ID));

        // when & then
        assertThatThrownBy(() -> userLoginService.refresh(OLD_TOKEN))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.REFRESH_TOKEN_REUSED);

        // 누가 진짜 주인인지 알 수 없으므로 전부 끊는다.
        verify(refreshTokenRepository).revokeAll(USER_ID);

        // 새 토큰을 내주면 안 된다.
        verify(refreshTokenRepository, never()).rotate(anyLong(), anyString());
    }

    @Test
    @DisplayName("만료됐거나 위조된 토큰이면 폐기 없이 인증 오류만 낸다")
    void refresh_unknownToken_doesNotRevoke() {
        // given: token에도 used에도 없다
        when(refreshTokenRepository.findUserId(OLD_TOKEN)).thenReturn(Optional.empty());
        when(refreshTokenRepository.findUsedUserId(OLD_TOKEN)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userLoginService.refresh(OLD_TOKEN))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        // 남의 토큰을 흉내 낸 요청으로 멀쩡한 세션을 끊을 수 있으면 안 된다.
        verify(refreshTokenRepository, never()).revokeAll(anyLong());
    }

    @Test
    @DisplayName("쿠키가 없어 토큰이 비어 있으면 인증 오류를 낸다")
    void refresh_noToken() {
        assertThatThrownBy(() -> userLoginService.refresh(null))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.UNAUTHORIZED);

        verify(refreshTokenRepository, never()).findUserId(anyString());
    }

    @Test
    @DisplayName("토큰은 유효해도 정지된 계정이면 재발급하지 않는다")
    void refresh_suspendedUser() {
        // given
        when(refreshTokenRepository.findUserId(OLD_TOKEN)).thenReturn(Optional.of(USER_ID));
        when(userMapper.findById(USER_ID)).thenReturn(Optional.of(parent(UserStatus.SUSPENDED)));

        // when & then
        assertThatThrownBy(() -> userLoginService.refresh(OLD_TOKEN))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_SUSPENDED);

        verify(refreshTokenRepository, never()).rotate(anyLong(), anyString());
    }

    private UserVo parent(UserStatus status) {
        return UserVo.builder()
                .id(USER_ID)
                .name("MeTest")
                .userType(UserType.PARENT)
                .role(UserRole.USER)
                .status(status)
                .build();
    }
}
