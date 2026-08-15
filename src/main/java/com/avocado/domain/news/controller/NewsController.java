package com.avocado.domain.news.controller;

import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.news.batch.NewsRssCrawlService;
import com.avocado.domain.news.dto.request.NewsAnswerRequestDto;
import com.avocado.domain.news.dto.response.NewsAnswerResponseDto;
import com.avocado.domain.news.dto.response.NewsDetailResponseDto;
import com.avocado.domain.news.dto.response.NewsListResponseDto;
import com.avocado.domain.news.service.NewsService;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import static com.avocado.global.response.code.SuccessCode.*;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final NewsRssCrawlService newsRssCrawlService;

    @GetMapping
    public ResponseEntity<ApiResponse<NewsListResponseDto>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Long childId,
            @RequestParam(required=false) Boolean completed,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        NewsListResponseDto data = newsService.getNewsList(page, size, childId,completed, authUser);

        return ResponseEntity
                .status(NEWS_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.success(NEWS_LIST_FOUND, data));
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponseDto>> getNewsDetail(
            @PathVariable Long newsId,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        NewsDetailResponseDto data = newsService.getNewsDetail(newsId, childId, authUser);

        return ResponseEntity
                .status(NEWS_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.success(NEWS_DETAIL_FOUND, data));
    }

    @PutMapping("/{newsId}/answers")
    public ResponseEntity<ApiResponse<NewsAnswerResponseDto>> saveAnswer(
            @PathVariable Long newsId,
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody NewsAnswerRequestDto request
    ) {
        NewsAnswerResponseDto data = newsService.saveAnswer(newsId, childId, authUser, request);

        return ResponseEntity
                .status(NEWS_ANSWER_SAVED.getHttpStatus())
                .body(ApiResponse.success(NEWS_ANSWER_SAVED, data));
    }

    //크롤링 테스트용 임시 엔드포인트. 테스트 후 삭제
    @PostMapping("/crawl")
    public ResponseEntity<String> crawl(){
        int savedCount = newsRssCrawlService.crawlAndSave();
        return ResponseEntity.ok(savedCount + "건 저장됨");
    }
}