package com.avocado.domain.transfer.mapper;

import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import org.apache.ibatis.annotations.Param;

public interface TransferRecipientMapper {

    TransferRecipientResponseDto findByUserCode(
            @Param("keyword") String keyword
    );

    TransferRecipientResponseDto findByAccountNumber(
            @Param("keyword") String keyword
    );
}
