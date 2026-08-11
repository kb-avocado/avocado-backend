package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserStatus;

/**
 * 다른 도메인이 회원 정보를 조회하거나 회원 상태를 바꿀 때 거치는 창구.
 *
 * 다른 도메인이 호출하는 기능만 둔다.
 * 로그인·회원가입처럼 user 도메인 안에서만 쓰는 기능은
 * UserLoginService, UserSignUpService처럼 목적별로 분리한다.
 */
public interface UserService {

    /**
     * 부모 회원의 계정 상태를 조회한다.
     *
     * @param userId 조회할 회원 ID
     * @return 부모 회원의 상태. 회원이 없거나 부모 계정이 아니면 null
     */
    UserStatus getParentStatus(Long userId);

    /**
     * 가입 절차를 마친 회원을 활성화한다.
     * 부모는 계좌를 연동했을 때, 아이는 가족 연결을 확정했을 때 호출한다.
     * 아직 가입 절차 중인(PENDING) 회원만 대상이다.
     *
     * @param userId 활성화할 회원 ID
     */
    void activate(Long userId);
}
