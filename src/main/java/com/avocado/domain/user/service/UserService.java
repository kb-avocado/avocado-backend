package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserVo;

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

    /**
     * 회원 ID로 회원 이름을 조회한다.
     *
     * @param userId 회원 ID
     * @return 회원 이름. 회원이 없으면 USER_NOT_FOUND 예외를 던진다.
     */
    String getUserName(Long userId);

    /**
     * 회원 ID로 회원 기본 정보를 조회한다.
     *
     * 회원 상태와 관계없이 조회한다.
     *
     * @param userId 조회할 회원 ID
     * @return 회원 기본 정보
     * @throws com.avocado.global.exception.BusinessException 회원이 없으면 USER_NOT_FOUND
     */
    UserVo getUser(
            Long userId
    );

    /**
     * 회원 ID로 ACTIVE 상태의 회원 기본 정보를 조회한다.
     *
     * 회원이 존재하더라도 상태가 ACTIVE가 아니면 조회하지 않는다.
     *
     * @param userId 조회할 회원 ID
     * @return ACTIVE 상태의 회원 기본 정보
     * @throws com.avocado.global.exception.BusinessException 회원이 없거나 ACTIVE 상태가 아니면 USER_NOT_ACTIVE
     */
    UserVo getActiveUser(
            Long userId
    );

    /**
     * 회원이 ACTIVE 상태의 부모 회원인지 검증한다.
     *
     * @param parentId 검증할 부모 회원 ID
     * @throws com.avocado.global.exception.BusinessException ACTIVE 부모 회원이 아닌 경우
     */
    void validateActiveParent(
            Long parentId
    );

    /**
     * 회원이 ACTIVE 상태의 자녀 회원인지 검증한다.
     *
     * @param childId 검증할 자녀 회원 ID
     * @throws com.avocado.global.exception.BusinessException ACTIVE 자녀 회원이 아닌 경우
     */
    void validateActiveChild(
            Long childId
    );
}
