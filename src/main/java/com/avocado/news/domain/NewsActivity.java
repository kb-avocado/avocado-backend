package com.avocado.news.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
@NoArgsConstructor
public class NewsActivity {
    private Long id;
    private Long childId;
    private Long articleId;
    private String childAnswer;
    private LocalDateTime viewedAt;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void markViewed() {
        if (this.viewedAt == null){
            this.viewedAt = LocalDateTime.now();
        }
    }

    public void saveAnswer(String childAnswer){
        this.childAnswer = childAnswer;
        this.completedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static NewsActivity createViewed(Long childId, Long articleId) {
        NewsActivity activity = new NewsActivity();
        activity.childId = childId;
        activity.articleId = articleId;
        activity.viewedAt = LocalDateTime.now();
        activity.createdAt = LocalDateTime.now();
        activity.updatedAt = LocalDateTime.now();
        return activity;
    }
}
