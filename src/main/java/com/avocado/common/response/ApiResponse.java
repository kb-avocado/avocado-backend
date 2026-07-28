package com.avocado.common.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ApiResponse<T> {
    private final boolean success;
    private final String code;
    private final String message;
    private final T data;

    public ApiResponse(
            boolean success,
            String code,
            String message,
            T data
    ) {
        this.success = success;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 데이터가 있는 성공 응답
    public static <T> ApiResponse<T> success(
            String code,
            String message,
            T data
    ) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code)
                .message(message)
                .data(data)
                .build();
    }

    // 데이터가 없는 성공 응답
    public static <T> ApiResponse<T> success(
            String code,
            String message
    ) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(code)
                .message(message)
                .data(null)
                .build();
    }
}
