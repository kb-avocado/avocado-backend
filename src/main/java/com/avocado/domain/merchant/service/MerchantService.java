package com.avocado.domain.merchant.service;

import com.avocado.domain.merchant.domain.MerchantVo;

import java.util.Optional;

/**
 * 결제 흐름에서 필요한 가맹점 기준 정보를 조회하는 서비스.
 * 현재는 POS 결제 검증을 위해 가맹점 존재 여부와 상태, 아이 결제 제한 여부를 제공한다.
 */
public interface MerchantService {

    /**
     * 가맹점 ID로 단건 조회한다.
     *
     * @param merchantId 조회할 가맹점 ID
     * @return 가맹점이 없으면 Optional.empty()
     */
    Optional<MerchantVo> findById(
            Long merchantId
    );
}
