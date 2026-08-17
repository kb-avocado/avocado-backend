package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.User;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.domain.UserVo;
import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.domain.user.domain.RefreshResult;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.user.repository.RefreshTokenRepository;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.global.util.EmailNormalizer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserLoginServiceImpl implements UserLoginService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * 이메일과 비밀번호로 로그인하고, 회원 타입에 맞는 화면 진입 정보를 함께 반환한다.
     *
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 실패 또는 로그인할 수 없는 계정 상태인 경우
     */
    @Override
    public LoginUserDto login(UserLoginRequestDto request) {
        // TODO: 이메일이 아이디로 대체되면 지우기 (정규화 없이 request 값을 그대로 조회)
        String email = EmailNormalizer.normalize(request.getEmail());

        User user = userMapper.selectByEmail(email);

        // 가입되지 않은 이메일과 비밀번호 불일치를 구분해서 응답하지 않는다.
        // 구분하면 어떤 이메일이 가입되어 있는지 알아낼 수 있다.
        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        UserVo userVo = toUserVo(user);

        validateLoginable(userVo);

        return userVo.getUserType() == UserType.PARENT
                ? toParentInfo(userVo)
                : toChildInfo(userVo);
    }

    /**
     * 토큰으로 로그인한 회원의 정보를 다시 조회한다.
     *
     * @param authUser 요청한 회원 (토큰에서 꺼낸 인증 주체)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 정보가 없거나, 회원이 없거나, 로그인할 수 없는 계정 상태인 경우
     */
    @Override
    public LoginUserDto me(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        UserVo user = userMapper.findById(authUser.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateLoginable(user);

        return user.getUserType() == UserType.PARENT
                ? toParentInfo(user)
                : toChildInfo(user);
    }

    // 로그인은 비밀번호 검증 때문에 User로 읽지만, 이후 조립은 UserVo만 있으면 된다.
    private UserVo toUserVo(User user) {
        return UserVo.builder()
                .id(user.getId())
                .name(user.getName())
                .userType(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }

    // PENDING은 로그인은 되지만 진입 화면이 다른 상태라 여기서 막지 않는다.
    private void validateLoginable(UserVo user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_DELETED);
        }
    }

    // 부모는 계좌가 연동되지 않았으면 PENDING이므로 accountId가 비어 있을 수 있다.
    private LoginUserDto toParentInfo(UserVo user) {
        return baseInfo(user)
                .accountId(userMapper.selectAccountIdByParentId(user.getId()))
                .child(userMapper.selectChildrenByParentId(user.getId()))
                .build();
    }

    // 아이는 부모와 연결되지 않았으면 PENDING이므로, 부모 ID 대신 연결 요청 정보를 내려준다.
    private LoginUserDto toChildInfo(UserVo user) {
        LoginUserDto.LoginUserDtoBuilder builder = baseInfo(user)
                .walletId(userMapper.selectWalletIdByChildId(user.getId()));

        if (user.getStatus() == UserStatus.PENDING) {
            builder.family(userMapper.selectFamilyByChildId(user.getId()));
        } else {
            builder.parentId(userMapper.selectParentIdByChildId(user.getId()));
        }

        return builder.build();
    }

    private LoginUserDto.LoginUserDtoBuilder baseInfo(UserVo user) {
        return LoginUserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .type(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus());
    }

    @Override
    public RefreshResult refresh(String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long userId = refreshTokenRepository.findUserId(refreshToken)
                .orElseGet(() -> {
                    // 유효하지 않은 토큰이라면, 이미 회전된 토큰인지 본다.
                    refreshTokenRepository.findUsedUserId(refreshToken)
                            .ifPresent(id -> {
                                // 토큰이 탈취되었으므로, 연결된 세션을 전부 끊는다.
                                refreshTokenRepository.revokeAll(id);
                                throw new BusinessException(ErrorCode.REFRESH_TOKEN_REUSED);
                            });
                    // 만료됐거나 위조된 토큰
                    throw new BusinessException(ErrorCode.UNAUTHORIZED);
                });

        UserVo user = userMapper.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        validateLoginable(user);

        String newRefreshToken = refreshTokenRepository.rotate(userId, refreshToken);

        LoginUserDto loginUser = user.getUserType() == UserType.PARENT
                ? toParentInfo(user)
                : toChildInfo(user);

        return RefreshResult.builder()
                .user(loginUser)
                .refreshToken(newRefreshToken)
                .build();
    }
}
