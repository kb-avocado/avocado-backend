package com.avocado.common.response;

import com.avocado.common.response.code.ResponseCode;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"success", "code", "message", "data"})
public class ApiResponse<T> {

    private final boolean success;

    private final String code;

    private final String message;

    private final T data;

    // 외부에서 생성자 호출 제한
    private ApiResponse(
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
    public static <T> ApiResponse<T> success(ResponseCode responseCode, T data) {
        return new ApiResponse<>(
                true,
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }

    // 데이터가 없는 성공 응답
    public static <T> ApiResponse<T> success(ResponseCode responseCode) {
        return new ApiResponse<>(
                true,
                responseCode.getCode(),
                responseCode.getMessage(),
                null
        );
    }

    // 기본 실패 응답
    public static <T> ApiResponse<T> failure(ResponseCode responseCode) {
        return new ApiResponse<>(
                false,
                responseCode.getCode(),
                responseCode.getMessage(),
                null
        );
    }

    // 상세 데이터를 포함하는 실패 응답
    public static <T> ApiResponse<T> failure(ResponseCode responseCode, T data) {
        return new ApiResponse<>(
                false,
                responseCode.getCode(),
                responseCode.getMessage(),
                data
        );
    }

}
