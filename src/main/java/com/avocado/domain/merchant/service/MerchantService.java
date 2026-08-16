package com.avocado.domain.merchant.service;

import com.avocado.domain.merchant.domain.MerchantVo;

import java.util.Optional;

public interface MerchantService {

    Optional<MerchantVo> findById(
            Long merchantId
    );
}
