package com.avocado.domain.user.domain;

import com.avocado.domain.user.dto.response.LoginUserDto;
import lombok.Builder;
import lombok.Getter;

/**
 * 재발급 결과. Controller가 쿠키 두 개를 구울 때 필요한 값을 담는다.
 * 응답 본문으로 나가지 않는다.
 */
@Getter
@Builder
public class RefreshResult {
    // 새 액세스 토큰을 만들 때 필요한 회원 정보
    private final LoginUserDto user;

    // 새 리프레시 쿠키에 담을 원문
    private final String refreshToken;
}
