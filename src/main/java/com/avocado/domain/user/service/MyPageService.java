package com.avocado.domain.user.service;

import com.avocado.domain.user.dto.response.MyPageResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

/**
 * 마이페이지 조회
 */
public interface MyPageService {

    /**
     * 로그인한 회원의 마이페이지 정보를 조회한다.
     *
     * @param authUser 요청한 회원 (토큰에서 꺼낸 인증 주체)
     * @return 회원 타입에 맞는 내 정보
     */
    MyPageResponseDto getMyPage(AuthUser authUser);
}
