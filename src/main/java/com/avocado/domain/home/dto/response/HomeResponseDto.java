package com.avocado.domain.home.dto.response;

import com.avocado.domain.news.dto.response.NewsListItemDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class HomeResponseDto {
    // 아이가 즐겨찾기 해둔 저금통 목록 ("나의 저금통" 섹션)
    private final List<HomeFavoritePiggyBankDto> favoritePiggyBanks;

    // 오늘 사용한 금액 ("리포트" 섹션)
    private final Long todaySpent;

    // 이번 달 사용한 금액 ("리포트" 섹션)
    private final Long monthSpent;

    // 가장 최근 신문 2건 ("경제가 쏙쏙" 섹션)
    private final List<NewsListItemDto> news;
}