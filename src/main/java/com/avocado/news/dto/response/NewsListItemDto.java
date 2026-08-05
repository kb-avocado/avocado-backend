package com.avocado.news.dto.response;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NewsListItemDto {
    private Long newsId;
    private String title;
    private String subtitle;
    private Boolean isNew;
    private Boolean isRead;
    private Boolean isCompleted;
    private LocalDateTime publishedAt;
}
