package com.avocado.common.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "API 공통 응답 포맷")
public class ApiResponse<T> {

    @ApiModelProperty(
            value = "성공 여부",
            example = "true",
            required = true
    )
    private final boolean success;

    @ApiModelProperty(
            value = "응답 코드",
            example = "SUCCESS",
            required = true
    )
    private final String code;

    @ApiModelProperty(
            value = "응답 메시지",
            example = "요청이 성공적으로 처리되었습니다.",
            required = true
    )
    private final String message;

    @ApiModelProperty(value = "응답 데이터 (없을 시 NULL)")
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
