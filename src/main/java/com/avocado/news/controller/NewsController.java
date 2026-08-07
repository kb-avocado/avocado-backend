package com.avocado.news.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.common.response.code.SuccessCode;
import com.avocado.news.batch.NewsRssCrawlService;
import com.avocado.news.dto.request.NewsAnswerRequestDto;
import com.avocado.news.dto.response.NewsAnswerResponseDto;
import com.avocado.news.dto.response.NewsDetailResponseDto;
import com.avocado.news.dto.response.NewsListResponseDto;
import com.avocado.news.service.NewsService;

import javax.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.avocado.common.response.code.SuccessCode.*;

// TODO: 로그인 붙으면 @AuthenticationPrincipal 등으로 childId를 토큰에서 꺼내도록 교체
// 지금은 데모용으로 임시 고정값 사용
@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private static final Long TEMP_CHILD_ID = 102L; // TODO: 로그인 붙으면 제거
    private final NewsRssCrawlService newsRssCrawlService;

    @GetMapping
    public ResponseEntity<ApiResponse<NewsListResponseDto>> getNewsList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        NewsListResponseDto data = newsService.getNewsList(page, size, TEMP_CHILD_ID);

        return ResponseEntity
                .status(NEWS_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.success(NEWS_LIST_FOUND, data));
    }

    @GetMapping("/{newsId}")
    public ResponseEntity<ApiResponse<NewsDetailResponseDto>> getNewsDetail(
            @PathVariable Long newsId
    ) {
        NewsDetailResponseDto data =
                newsService.getNewsDetail(newsId, TEMP_CHILD_ID);

        return ResponseEntity
                .status(NEWS_DETAIL_FOUND.getHttpStatus())
                .body(ApiResponse.success(NEWS_DETAIL_FOUND, data));
    }

    @PutMapping("/{newsId}/answers")
    public ResponseEntity<ApiResponse<NewsAnswerResponseDto>> saveAnswer(
            @PathVariable Long newsId,
            @Valid @RequestBody NewsAnswerRequestDto request
    ) {
        NewsAnswerResponseDto data = newsService.saveAnswer(newsId, TEMP_CHILD_ID, request);

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