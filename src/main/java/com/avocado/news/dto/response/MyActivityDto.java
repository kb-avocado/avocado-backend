package com.avocado.news.dto.response;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyActivityDto {
    private String childAnswer;
    private LocalDateTime viewedAt;
    private LocalDateTime completedAt;
}
