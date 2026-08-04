// news/service/NewsService.java
package com.avocado.news.service;

import com.avocado.news.dto.request.NewsAnswerRequestDto;
import com.avocado.news.dto.response.NewsAnswerResponseDto;
import com.avocado.news.dto.response.NewsDetailResponseDto;
import com.avocado.news.dto.response.NewsListResponseDto;

public interface NewsService {
    NewsListResponseDto getNewsList(int page, int size);
    NewsDetailResponseDto getNewsDetail(Long newsId, Long childId);
    NewsAnswerResponseDto saveAnswer(Long newsId, Long childId, NewsAnswerRequestDto request);
}