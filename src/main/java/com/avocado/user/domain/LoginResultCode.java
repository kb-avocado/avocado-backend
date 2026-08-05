package com.avocado.user.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 로그인 성공 응답의 code / message.
 * 인증은 성공했지만 아직 준비가 덜 된 계정(PENDING)은 프론트에서 홈이 아닌
 * 다른 화면으로 보내야 하므로, 부모(계좌 미연결)와 아이(가족 미연결)를 구분한다.
 */
@Getter
@AllArgsConstructor
public enum LoginResultCode {

    LOGIN_SUCCESS(
            "LOGIN_SUCCESS",
            "로그인에 성공했습니다."
    ),

    ACCOUNT_LINK_REQUIRED(
            "ACCOUNT_LINK_REQUIRED",
            "로그인에 성공했습니다. 계좌 연결이 필요합니다."
    ),

    FAMILY_LINK_REQUIRED(
            "FAMILY_LINK_REQUIRED",
            "로그인에 성공했습니다. 가족 연결이 필요합니다."
    );

    private final String code;
    private final String message;

    public static LoginResultCode of(
            UserType type,
            UserStatus status
    ) {
        if (status != UserStatus.PENDING) {
            return LOGIN_SUCCESS;
        }

        return type == UserType.PARENT
                ? ACCOUNT_LINK_REQUIRED
                : FAMILY_LINK_REQUIRED;
    }
}
