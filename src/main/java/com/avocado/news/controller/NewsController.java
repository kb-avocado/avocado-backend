package com.avocado.news.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.news.dto.request.NewsAnswerRequestDto;
import com.avocado.news.dto.response.NewsAnswerResponseDto;
import com.avocado.news.dto.response.NewsDetailResponseDto;
import com.avocado.news.dto.response.NewsListResponseDto;
import com.avocado.news.service.NewsService;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

// TODO: 로그인 붙으면 @AuthenticationPrincipal 등으로 childId를 토큰에서 꺼내도록 교체
// 지금은 데모용으로 임시 고정값 사용
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private static final Long TEMP_CHILD_ID = 12L; // TODO: 로그인 붙으면 제거

    @GetMapping
    public ApiResponse<NewsListResponseDto> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        NewsListResponseDto data = newsService.getNewsList(page, size);
        return ApiResponse.success("NEWS_LIST_FOUND", "신문 목록을 조회했습니다.", data);
    }

    @GetMapping("/{newsId}")
    public ApiResponse<NewsDetailResponseDto> getNewsDetail(@PathVariable Long newsId) {
        NewsDetailResponseDto data = newsService.getNewsDetail(newsId, TEMP_CHILD_ID);
        return ApiResponse.success("NEWS_DETAIL_FOUND", "신문 상세 정보를 조회했습니다.", data);
    }

    @PutMapping("/{newsId}/answers")
    public ApiResponse<NewsAnswerResponseDto> saveAnswer(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsAnswerRequestDto request
    ) {
        NewsAnswerResponseDto data = newsService.saveAnswer(newsId, TEMP_CHILD_ID, request);
        return ApiResponse.success("NEWS_ANSWER_SAVED", "챌린지 답변이 저장되었습니다.", data);
    }
}