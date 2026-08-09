package com.avocado.domain.news.dto.response;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NewsAnswerResponseDto {
    private Long newsId;
    private String childAnswer;
    private LocalDateTime completedAt;
}
