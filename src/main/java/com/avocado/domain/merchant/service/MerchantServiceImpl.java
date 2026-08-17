package com.avocado.domain.merchant.service;

import com.avocado.domain.merchant.domain.MerchantVo;
import com.avocado.domain.merchant.mapper.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MerchantServiceImpl implements MerchantService {

    private final MerchantMapper merchantMapper;

    @Override
    public Optional<MerchantVo> findById(
            Long merchantId
    ) {
        return merchantMapper.findById(merchantId);
    }
}
