package com.avocado.domain.user.service;

import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.global.exception.BusinessException;

public interface UserLoginService {

    /**
     * 이메일과 비밀번호로 로그인하고, 회원 타입에 맞는 화면 진입 정보를 함께 반환한다.
     *
     * @param request 로그인 요청 (이메일, 비밀번호)
     * @return 로그인한 회원 정보
     * @throws BusinessException 인증 실패 또는 로그인할 수 없는 계정 상태인 경우
     */
    LoginUserDto login(UserLoginRequestDto request);
}
