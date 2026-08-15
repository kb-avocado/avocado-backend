// news/service/NewsService.java
package com.avocado.domain.news.service;

import com.avocado.domain.news.dto.request.NewsAnswerRequestDto;
import com.avocado.domain.news.dto.response.NewsAnswerResponseDto;
import com.avocado.domain.news.dto.response.NewsDetailResponseDto;
import com.avocado.domain.news.dto.response.NewsListResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface NewsService {
    NewsListResponseDto getNewsList(int page, int size, Long childId, AuthUser authUser);
    NewsDetailResponseDto getNewsDetail(Long newsId, Long childId, AuthUser authUser);
    NewsAnswerResponseDto saveAnswer(Long newsId, Long childId, AuthUser authUser, NewsAnswerRequestDto request);
}