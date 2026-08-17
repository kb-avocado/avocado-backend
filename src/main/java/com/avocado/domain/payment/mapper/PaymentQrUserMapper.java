package com.avocado.domain.payment.mapper;

import com.avocado.domain.user.domain.UserVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PaymentQrUserMapper {

    List<UserVo> findNamesByIds(
            @Param("userIds") List<Long> userIds
    );
}
