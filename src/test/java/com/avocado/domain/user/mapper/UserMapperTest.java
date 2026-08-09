package com.avocado.domain.user.mapper;

import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.global.config.MyBatisConfig;
import com.avocado.global.config.RootConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        RootConfig.class,
        MyBatisConfig.class
})
@Transactional
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("활성 상태인 부모 회원이 존재할 경우 true를 반환")
    public void existsActiveParentById_returnTrue() {
        // given
        Long userId = 101L;

        // when
        boolean existed = userMapper.existsActiveParentById(userId);

        // then
        assertThat(existed).isTrue();
    }

    @Test
    @DisplayName("활성 상태인 부모 회원이 존재하지 않을 경우 false를 반환")
    public void existsActiveParentById_returnFalse() {
        // given
        Long userId = 999L;

        // when
        boolean existed = userMapper.existsActiveParentById(userId);

        // then
        assertThat(existed).isFalse();
    }
}