package com.avocado.common.exception;

import com.avocado.common.response.ApiResponse;import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final boolean success;
    private final String code;
    private final String message;

    // 정적 팩토리 메서드
    public static ApiResponse.ErrorResponse from(ErrorCode errorCode) {
        return new ApiResponse.ErrorResponse(
                false,
                errorCode.getCode(),
                errorCode.getMessage()
        );
    }
}
