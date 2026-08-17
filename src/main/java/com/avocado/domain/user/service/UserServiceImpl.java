package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserVo;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 부모 회원의 계정 상태를 조회한다.
     *
     * @param userId 조회할 회원 ID
     * @return 부모 회원의 상태. 회원이 없거나 부모 계정이 아니면 null
     */
    @Override
    @Transactional(readOnly = true)
    public UserStatus getParentStatus(Long userId) {
        return userMapper.selectParentStatusById(userId);
    }

    /**
     * 가입 절차를 마친 회원을 활성화한다.
     * PENDING이 아닌 회원의 상태는 바꾸지 않는다.
     *
     * @param userId 활성화할 회원 ID
     */
    @Override
    @Transactional
    public void activate(Long userId) {
        userMapper.updateStatus(userId, UserStatus.PENDING, UserStatus.ACTIVE);
    }

    /**
     * 회원 ID로 회원 이름을 조회한다.
     *
     * @param userId 회원 ID
     * @return 회원 이름
     */
    @Override
    @Transactional(readOnly = true)
    public String getUserName(Long userId) {
        return userMapper
                .findNameById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    /**
     * 회원 ID로 회원 기본 정보를 조회한다.
     * <p>
     * 회원 상태와 관계없이 조회한다.
     *
     * @param userId 조회할 회원 ID
     * @return 회원 기본 정보
     */
    @Override
    public UserVo getUser(
            Long userId
    ) {
        return userMapper
                .findById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_FOUND));
    }

    /**
     * 회원 ID로 ACTIVE 상태의 회원 기본 정보를 조회한다.
     * <p>
     * 회원이 없거나 ACTIVE 상태가 아니면
     * USER_NOT_ACTIVE 예외를 발생시킨다.
     *
     * @param userId 조회할 회원 ID
     * @return ACTIVE 상태의 회원 기본 정보
     */
    @Override
    public UserVo getActiveUser(
            Long userId
    ) {
        return userMapper
                .findActiveById(userId)
                .orElseThrow(() -> new BusinessException(USER_NOT_ACTIVE));
    }

    /**
     * 회원이 ACTIVE 상태의 부모 회원인지 검증한다.
     *
     * @param parentId 검증할 부모 회원 ID
     * @throws BusinessException ACTIVE 부모 회원이 아닌 경우
     */
    @Override
    public void validateActiveParent(
            Long parentId
    ) {
        // ACTIVE 부모 회원인지 확인한다.
        boolean isActiveParent = userMapper.existsActiveParentById(parentId);

        // 유효한 부모 회원이 아니면 예외를 발생시킨다.
        if (!isActiveParent) {
            throw new BusinessException(NOT_PARENT_USER);
        }
    }

    /**
     * 회원이 ACTIVE 상태의 자녀 회원인지 검증한다.
     *
     * @param childId 검증할 자녀 회원 ID
     * @throws BusinessException ACTIVE 자녀 회원이 아닌 경우
     */
    @Override
    public void validateActiveChild(
            Long childId
    ) {
        // ACTIVE 자녀 회원인지 확인한다.
        boolean isActiveChild = userMapper.existsActiveChildById(childId);

        // 유효한 자녀 회원이 아니면 예외를 발생시킨다.
        if (!isActiveChild) {
            throw new BusinessException(NOT_CHILD_USER);
        }
    }
}
