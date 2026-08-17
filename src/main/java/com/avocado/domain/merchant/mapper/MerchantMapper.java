package com.avocado.domain.merchant.mapper;

import com.avocado.domain.merchant.domain.MerchantVo;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

public interface MerchantMapper {

    Optional<MerchantVo> findById(
            @Param("merchantId") Long merchantId
    );
}
