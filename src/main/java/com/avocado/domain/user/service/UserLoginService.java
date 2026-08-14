package com.avocado.domain.user.service;

import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.domain.user.domain.RefreshResult;
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

    /**
     * Refresh Token으로 새 토큰 한 쌍을 발급한다.
     * 회전 방식 - Refresh Token도 매번 새 것으로 바뀐다.
     * 이미 회전된 토큰이 다시 오면 탈취로 보고 해당 회원의 모든 세션을 끊는다.
     *
     * @param refreshToken 쿠키에서 꺼낸 토큰 원문
     * @return 새로 발급할 토큰 정보 (회원 정보 + 새 리프레시 토큰)
     * @throws BusinessException 토큰이 없거나, 만료됐거나, 재사용이 감지된 경우
     */
    RefreshResult refresh(String refreshToken);
}
