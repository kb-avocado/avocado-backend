package com.avocado.common.response;

import com.avocado.common.response.code.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class ErrorResponse {

    private final String code;
    private final String message;

    // 기본 에러 응답
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // 커스텀 메시지 사용 에러 응답
    public static ErrorResponse of(
            ErrorCode errorCode,
            String message
    ) {
        return ErrorResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();
    }
}
