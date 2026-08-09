package com.avocado.domain.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 샘플 계정용 BCrypt 해시를 생성하기 위한 임시 유틸.
 * Avocado_AddData.sql의 비밀번호를 실제 해시로 교체할 때 사용한다.
 * BCrypt는 salt가 매번 달라 실행할 때마다 결과가 다르며, 검증은 matches()가 처리한다.
 */
class PasswordHashPrinter {

    private static final String RAW_PASSWORD = "avocado1234";

    @Test
    void printHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encoded = encoder.encode(RAW_PASSWORD);

        System.out.println("raw     = " + RAW_PASSWORD);
        System.out.println("encoded = " + encoded);
    }
}
