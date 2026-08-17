package com.avocado.domain.home.dto.response;

import lombok.Builder;
import lombok.Getter;

// 홈 화면 "나의 저금통" 섹션에 노출하는, 아이가 즐겨찾기 해둔 저금통 하나에 대한 응답.
@Getter
@Builder
public class HomeFavoritePiggyBankDto {
    private final Long piggyBankId;
    private final String name;
    private final String icon;
    private final Long balance;
    private final Long targetAmount;
    private final Integer progressRate;
}