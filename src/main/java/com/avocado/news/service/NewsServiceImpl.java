package com.avocado.news.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.news.domain.NewsActivity;
import com.avocado.news.domain.NewsArticle;
import com.avocado.news.dto.request.NewsAnswerRequestDto;
import com.avocado.news.dto.response.NewsAnswerResponseDto;
import com.avocado.news.dto.response.NewsDetailResponseDto;
import com.avocado.news.dto.response.NewsListItemDto;
import com.avocado.news.dto.response.NewsListResponseDto;
import com.avocado.news.mapper.NewsActivityMapper;
import com.avocado.news.mapper.NewsArticleMapper;
import com.avocado.news.mapper.NewsConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsActivityMapper newsActivityMapper;
    private final NewsConverter newsConverter;

    @Override
    public NewsListResponseDto getNewsList(int page, int size) {
        int offset = page * size;
        List<NewsArticle> articles = newsArticleMapper.findList(offset, size);
        long totalCount = newsArticleMapper.countAll();

        List<NewsListItemDto> items = articles.stream()
                .map(newsConverter::toListItemDto)
                .toList();

        return NewsListResponseDto.builder()
                .totalCount(totalCount)
                .news(items)
                .build();
    }

    @Override
    @Transactional
    public NewsDetailResponseDto getNewsDetail(Long newsId, Long childId) {
        NewsArticle article = newsArticleMapper.findById(newsId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);
        }

        NewsActivity activity = newsActivityMapper.findByChildIdAndArticleId(childId, newsId);

        if (activity == null) {
            activity = NewsActivity.createViewed(childId, newsId);
            newsActivityMapper.insert(activity);
        } else if (activity.getViewedAt() == null) {
            activity.markViewed();
            newsActivityMapper.update(activity);
        }

        return newsConverter.toDetailDto(article, activity);
    }

    @Override
    @Transactional
    public NewsAnswerResponseDto saveAnswer(Long newsId, Long childId, NewsAnswerRequestDto request) {
        NewsActivity activity = newsActivityMapper.findByChildIdAndArticleId(childId, newsId);

        if (activity == null) {
            activity = NewsActivity.createViewed(childId, newsId);
            activity.saveAnswer(request.getChildAnswer());
            newsActivityMapper.insert(activity);
        } else {
            activity.saveAnswer(request.getChildAnswer());
            newsActivityMapper.update(activity);
        }

        return newsConverter.toAnswerResponseDto(activity);
    }
}