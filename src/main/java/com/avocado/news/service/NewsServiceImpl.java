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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsActivityMapper newsActivityMapper;
    private final NewsConverter newsConverter;

    @Override
    public NewsListResponseDto getNewsList(int page, int size, Long childId) {
        int offset = page * size;
        List<NewsArticle> articles = newsArticleMapper.findList(offset, size);
        long totalCount = newsArticleMapper.countAll();

        List<Long> articleIds = articles.stream()
                .map(NewsArticle::getId)
                .toList();

        Map<Long, NewsActivity> activityByArticleId = articleIds.isEmpty()
                ? Collections.emptyMap()
                : newsActivityMapper.findByChildIdAndArticleIds(childId, articleIds).stream()
                .collect(Collectors.toMap(NewsActivity::getArticleId, Function.identity()));

        List<NewsListItemDto> items = articles.stream()
                .map(article -> newsConverter.toListItemDto(
                        article,
                        activityByArticleId.get(article.getId())
                ))
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