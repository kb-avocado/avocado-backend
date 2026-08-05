// news/domain/NewsArticle.java
package com.avocado.news.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class NewsArticle {
    private Long id;
    private String title;
    private String subtitle;
    private String link;
    private String todayChallenge;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
}