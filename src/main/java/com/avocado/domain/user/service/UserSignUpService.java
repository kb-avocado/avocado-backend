package com.avocado.domain.user.service;

import com.avocado.domain.user.dto.request.UserSignUpRequestDto;
import com.avocado.domain.user.dto.response.UserSignUpResponseDto;
import com.avocado.global.exception.BusinessException;

public interface UserSignUpService {

    /**
     * 회원을 등록하고, 가입 직후 이동할 화면을 정하는 데 필요한 정보를 반환한다.
     *
     * @param request 회원가입 요청 (이름, 이메일, 비밀번호, 전화번호, 생년월일, 회원 타입)
     * @return 가입한 회원 정보
     * @throws BusinessException 이메일 또는 전화번호가 이미 사용 중인 경우
     */
    UserSignUpResponseDto signUp(UserSignUpRequestDto request);
}
