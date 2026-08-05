package com.avocado.common.response.code;

import org.springframework.http.HttpStatus;

// 성공 코드와 실패 코드가 해당 인터페이스를 구현
public interface ResponseCode {

    // HTTP 상태 코드를 반환
    HttpStatus getHttpStatus();

    // 서비스 응답 코드를 반환
    String getCode();

    // 기본 메시지를 반환
    String getMessage();

}
