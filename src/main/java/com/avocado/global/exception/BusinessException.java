package com.avocado.global.exception;

import com.avocado.global.response.code.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    // 서비스 계층에서는 비즈니스 조건을 만족하지 못했을 때 해당 예외를 발생.
    // 예시 코드.
    // throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
}
