package com.avocado.global.util;

import java.util.Locale;

/**
 * TODO: 이메일이 아이디로 대체되면 지우기
 * 이메일을 저장/조회에 쓸 하나의 형태로 맞춘다.
 */
public final class EmailNormalizer {
    private EmailNormalizer() {
    }
    /**
     * 앞뒤 공백을 없애고 소문자로 바꾼다.
     *
     * @param email 사용자가 입력한 이메일
     * @return 정규화된 이메일. email이 null이면 null
     */
    public static String normalize(String email) {
        if (email == null) {
            return null;
        }

        return email.trim().toLowerCase(Locale.ROOT);
    }
}
