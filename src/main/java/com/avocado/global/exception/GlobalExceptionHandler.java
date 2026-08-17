package com.avocado.global.exception;

import com.avocado.global.response.ErrorResponse;
import com.avocado.global.response.code.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static com.avocado.global.response.code.ErrorCode.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 서비스에서 의도적으로 발생시킨 비즈니스 예외
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        log.warn(
                "BusinessException occurred: code = {}, status = {}, message = {}",
                errorCode.getCode(),
                errorCode.getHttpStatus(),
                errorCode.getMessage()
        );

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.of(errorCode));
    }

    // @RequestBody + @Valid 검증 실패 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String field = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getField())
                .orElse("unknown");

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(INVALID_REQUEST.getMessage());

        log.warn(
                "Request validation failed: field = {}, message = {}",
                field,
                message
        );

        return ResponseEntity
                .status(INVALID_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(INVALID_REQUEST, message));
    }

    /**
     * 쿼리 파라미터 DTO의 바인딩 및 검증 실패를 처리한다.
     *
     * @param e 바인딩 및 검증 실패 예외
     * @return 잘못된 요청 응답
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> handleBindException(BindException e) {
        String field = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getField())
                .orElse("unknown");

        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(fieldError -> fieldError.getDefaultMessage())
                .orElse(INVALID_REQUEST.getMessage());

        log.warn(
                "Request parameter validation failed: field = {}, message = {}",
                field,
                message
        );

        return ResponseEntity
                .status(INVALID_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(INVALID_REQUEST, message));
    }

    // JSON 형식 오류 또는 타입 변환 실패 예외
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn(
                "Request body parsing failed: message = {}",
                e.getMessage()
        );


        return ResponseEntity
                .status(INVALID_REQUEST.getHttpStatus())
                .body(ErrorResponse.of(INVALID_REQUEST));
    }

    // 처리하지 못한 모든 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error(
                "Unhandled exception occurred: type = {}, message = {}",
                e.getClass().getSimpleName(),
                e.getMessage(),
                e
        );

        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR.getHttpStatus())
                .body(ErrorResponse.of(INTERNAL_SERVER_ERROR));
    }
}
