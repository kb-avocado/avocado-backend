package com.avocado.news.dto.response;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NewsDetailResponseDto {
    private Long newsId;
    private String title;
    private String subtitle;
    private String link;
    private String todayChallenge;
    private LocalDateTime publishedAt;
    private MyActivityDto myActivity;
}
