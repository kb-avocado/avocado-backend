package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.User;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
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

    /**
     * 이메일과 비밀번호로 로그인하고, 회원 타입에 맞는 화면 진입 정보를 함께 반환한다.
     *
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 실패 또는 로그인할 수 없는 계정 상태인 경우
     */
    @Override
    public LoginUserDto login(UserLoginRequestDto request) {
        User user = userMapper.selectByEmail(request.getEmail());

        // 가입되지 않은 이메일과 비밀번호 불일치를 구분해서 응답하지 않는다.
        // 구분하면 어떤 이메일이 가입되어 있는지 알아낼 수 있다.
        if (user == null
                || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        validateLoginable(user);

        return user.getUserType() == UserType.PARENT
                ? toParentInfo(user)
                : toChildInfo(user);
    }

    // PENDING은 로그인은 되지만 진입 화면이 다른 상태라 여기서 막지 않는다.
    private void validateLoginable(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED) {
            throw new BusinessException(ErrorCode.USER_SUSPENDED);
        }

        if (user.getStatus() == UserStatus.DELETED) {
            throw new BusinessException(ErrorCode.USER_DELETED);
        }
    }

    // 부모는 계좌가 연동되지 않았으면 PENDING이므로 accountId가 비어 있을 수 있다.
    private LoginUserDto toParentInfo(User user) {
        return baseInfo(user)
                .accountId(userMapper.selectAccountIdByParentId(user.getId()))
                .child(userMapper.selectChildrenByParentId(user.getId()))
                .build();
    }

    // 아이는 부모와 연결되지 않았으면 PENDING이므로, 부모 ID 대신 연결 요청 정보를 내려준다.
    private LoginUserDto toChildInfo(User user) {
        LoginUserDto.LoginUserDtoBuilder builder = baseInfo(user)
                .walletId(userMapper.selectWalletIdByChildId(user.getId()));

        if (user.getStatus() == UserStatus.PENDING) {
            builder.family(userMapper.selectFamilyByChildId(user.getId()));
        } else {
            builder.parentId(userMapper.selectParentIdByChildId(user.getId()));
        }

        return builder.build();
    }

    private LoginUserDto.LoginUserDtoBuilder baseInfo(User user) {
        return LoginUserDto.builder()
                .userId(user.getId())
                .name(user.getName())
                .type(user.getUserType())
                .role(user.getRole())
                .status(user.getStatus());
    }
}
