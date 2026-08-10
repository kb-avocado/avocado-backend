package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserStatus;

/**
 * 회원 계정 상태를 다루는 창구.
 * 계좌 연동(account)과 가족 연결(family)이 끝나면 회원이 활성화되는데,
 * 그 판단 규칙이 각 도메인에 흩어지지 않도록 이곳에 모은다.
 */
public interface UserStatusService {

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
