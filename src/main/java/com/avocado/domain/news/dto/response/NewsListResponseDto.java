package com.avocado.domain.news.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class NewsListResponseDto {
    private long totalCount;
    private List<NewsListItemDto> news;

}
