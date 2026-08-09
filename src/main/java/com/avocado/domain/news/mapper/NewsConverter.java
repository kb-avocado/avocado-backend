// news/mapper/NewsConverter.java
package com.avocado.domain.news.mapper;

import com.avocado.domain.news.domain.NewsActivity;
import com.avocado.domain.news.domain.NewsArticle;
import com.avocado.domain.news.dto.response.MyActivityDto;
import com.avocado.domain.news.dto.response.NewsAnswerResponseDto;
import com.avocado.domain.news.dto.response.NewsDetailResponseDto;
import com.avocado.domain.news.dto.response.NewsListItemDto;
import com.avocado.domain.news.dto.response.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class NewsConverter {

    private static final int NEW_BADGE_DAYS = 3;

    public NewsListItemDto toListItemDto(NewsArticle article, NewsActivity activity) {
        boolean isRecentlyCreated = article.getCreatedAt()
                .isAfter(LocalDateTime.now().minusDays(NEW_BADGE_DAYS));
        boolean isUnread = activity == null || activity.getViewedAt() == null;
        boolean isNew = isRecentlyCreated && isUnread;

        return NewsListItemDto.builder()
                .newsId(article.getId())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .isNew(isNew)
                .isRead(activity != null && activity.getViewedAt() != null)
                .isCompleted(activity != null && activity.getCompletedAt() != null)
                .publishedAt(article.getPublishedAt())
                .build();
    }

    public NewsDetailResponseDto toDetailDto(NewsArticle article, NewsActivity activity) {
        return NewsDetailResponseDto.builder()
                .newsId(article.getId())
                .title(article.getTitle())
                .subtitle(article.getSubtitle())
                .link(article.getLink())
                .todayChallenge(article.getTodayChallenge())
                .publishedAt(article.getPublishedAt())
                .myActivity(activity.getChildAnswer() == null ? null : toMyActivityDto(activity))
                .build();
    }

    private MyActivityDto toMyActivityDto(NewsActivity activity) {
        return MyActivityDto.builder()
                .childAnswer(activity.getChildAnswer())
                .viewedAt(activity.getViewedAt())
                .completedAt(activity.getCompletedAt())
                .build();
    }

    public NewsAnswerResponseDto toAnswerResponseDto(NewsActivity activity) {
        return NewsAnswerResponseDto.builder()
                .newsId(activity.getArticleId())
                .childAnswer(activity.getChildAnswer())
                .completedAt(activity.getCompletedAt())
                .build();
    }
}