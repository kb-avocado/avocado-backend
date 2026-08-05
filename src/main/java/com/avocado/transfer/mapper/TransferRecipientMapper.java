package com.avocado.transfer.mapper;

import com.avocado.transfer.dto.response.TransferRecipientResponseDto;
import org.apache.ibatis.annotations.Param;

public interface TransferRecipientMapper {

    TransferRecipientResponseDto findByUserCode(
            @Param("keyword") String keyword
    );

    TransferRecipientResponseDto findByAccountNumber(
            @Param("keyword") String keyword
    );
}
