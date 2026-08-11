package com.avocado.domain.user.service;

import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    /**
     * 부모 회원의 계정 상태를 조회한다.
     *
     * @param userId 조회할 회원 ID
     * @return 부모 회원의 상태. 회원이 없거나 부모 계정이 아니면 null
     */
    @Override
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
}
