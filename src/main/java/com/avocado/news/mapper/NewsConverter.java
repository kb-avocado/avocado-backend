// news/mapper/NewsConverter.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsActivity;
import com.avocado.news.domain.NewsArticle;
import com.avocado.news.dto.response.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NewsConverter {

    private static final int NEW_BADGE_DAYS = 3;

    public NewsListItemDto toListItemDto(NewsArticle article) {
        boolean isNew = article.getPublishedAt()
                .isAfter(LocalDateTime.now().minusDays(NEW_BADGE_DAYS));

        return NewsListItemDto.builder()
                .newsId(article.getId())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .isNew(isNew)
                .publishedAt(article.getPublishedAt())
                .build();
    }

    public NewsDetailResponseDto toDetailDto(NewsArticle article, NewsActivity activity) {
        return NewsDetailResponseDto.builder()
                .newsId(article.getId())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .link(article.getLink())
                .challengeQuestion(article.getChallengeQuestion())
                .publishedAt(article.getPublishedAt())
                .myActivity(activity.getChildAnswer() == null ? null : toMyActivityDto(activity))
                .build();
    }

    private MyActivityDto toMyActivityDto(NewsActivity activity) {
        return MyActivityDto.builder()
                .childAnswer(activity.getChildAnswer())
                .isCompleted(activity.getIsCompleted())
                .viewedAt(activity.getViewedAt())
                .completedAt(activity.getCompletedAt())
                .build();
    }

    public NewsAnswerResponseDto toAnswerResponseDto(NewsActivity activity) {
        return NewsAnswerResponseDto.builder()
                .newsId(activity.getArticleId())
                .childAnswer(activity.getChildAnswer())
                .isCompleted(activity.getIsCompleted())
                .completedAt(activity.getCompletedAt())
                .build();
    }
}