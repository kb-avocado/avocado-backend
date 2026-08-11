package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.dto.response.MyPageResponseDto;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.avocado.global.response.code.ErrorCode.UNAUTHORIZED;
import static com.avocado.global.response.code.ErrorCode.USER_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageServiceImpl implements MyPageService {

    private final UserMapper userMapper;

    /**
     * 로그인한 회원의 마이페이지 정보를 조회한다.
     *
     * @param authUser 요청한 회원 (토큰에서 꺼낸 인증 주체)
     * @return 회원 타입에 맞는 내 정보
     * @throws BusinessException 인증 정보가 없거나 조회할 회원이 없는 경우
     */
    @Override
    public MyPageResponseDto getMyPage(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        MyPageResponseDto myPage = authUser.getUserType() == UserType.PARENT
                ? userMapper.selectParentMyPageById(authUser.getUserId())
                : userMapper.selectChildMyPageById(authUser.getUserId());

        if (myPage == null) {
            throw new BusinessException(USER_NOT_FOUND);
        }

        return myPage;
    }
}
