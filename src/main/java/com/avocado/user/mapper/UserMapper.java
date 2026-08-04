package com.avocado.user.mapper;

import com.avocado.user.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    // 로그인 시 이메일로 회원 조회 (email은 UNIQUE)
    User selectByEmail(@Param("email") String email);
}
