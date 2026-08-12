package com.avocado.domain.user.service;

import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface UserLoginService {

    /**
     * 이메일과 비밀번호로 로그인하고, 회원 타입에 맞는 화면 진입 정보를 함께 반환한다.
     *
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 실패 또는 로그인할 수 없는 계정 상태인 경우
     */
    LoginUserDto login(UserLoginRequestDto request);

    /**
     * 토큰으로 로그인한 회원의 정보를 다시 조회한다.
     *
     * Access Token은 HttpOnly 쿠키라 브라우저 JS가 읽을 수 없어서,
     * 새로고침하면 화면에 있던 회원 정보가 사라진다. 이때 복원용으로 쓴다.
     *
     * @param authUser 요청한 회원 (토큰에서 꺼낸 인증 주체)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 정보가 없거나, 회원이 없거나, 로그인할 수 없는 계정 상태인 경우
     */
    LoginUserDto me(AuthUser authUser);
}
