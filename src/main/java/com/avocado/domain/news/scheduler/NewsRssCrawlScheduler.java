package com.avocado.domain.news.scheduler;

import com.avocado.domain.news.batch.NewsRssCrawlService;
import com.avocado.domain.news.mapper.NewsArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsRssCrawlScheduler {

    /**
     * 초기 RSS 전체 적재 완료 판단 기준.
     * RSS 전체 조회 시 약 50건 정도가 내려오므로
     * 45건 이상이면 초기 적재가 완료된 것으로 본다.
     */
    private static final long INITIAL_ARTICLE_COUNT = 45L;

    /**
     * 뉴스 RSS 갱신 주기.
     */
    private static final long CRAWL_INTERVAL_DAYS = 7L;

    private static final String KST_ZONE = "Asia/Seoul";
    private static final ZoneId KST = ZoneId.of(KST_ZONE);

    private final NewsRssCrawlService newsRssCrawlService;
    private final NewsArticleMapper newsArticleMapper;

    /**
     * 매일 오전 6시에 스케줄러가 실행된다.
     *
     * 단, 실제 RSS 크롤링은
     *
     * 1. news_articles가 45건 이상이고
     * 2. 가장 최근 기사 적재일로부터 지난 날짜가
     *    7일, 14일, 21일...처럼 7일 단위일 때만 수행한다.
     */
    @Scheduled(
            cron = "0 0 6 * * *",
            zone = KST_ZONE
    )
// 기능 잘 되는지 테스트를 위한 매 분 0초마다 도는 스케줄러
//    @Scheduled(
//            cron = "0 * * * * *",
//            zone = "Asia/Seoul"
//    )
    public void crawlWeeklyNews() {

        // 1. 초기 뉴스 적재가 되어 있는지 확인
        long articleCount = newsArticleMapper.countArticles();

        if (articleCount < INITIAL_ARTICLE_COUNT) {
            log.info(
                    "뉴스 정기 크롤링 대기 - 현재 기사 {}건 / 기준 {}건",
                    articleCount,
                    INITIAL_ARTICLE_COUNT
            );
            return;
        }

        // 2. 가장 최근 뉴스 적재 시각 조회
        LocalDateTime latestCreatedAt =
                newsArticleMapper.findLatestCreatedAt();

        if (latestCreatedAt == null) {
            log.warn(
                    "최근 뉴스 적재 시각을 찾을 수 없어 크롤링을 건너뜁니다."
            );
            return;
        }

        LocalDate latestCreatedDate =
                latestCreatedAt.toLocalDate();

        LocalDate today =
                LocalDate.now(KST);

        // 3. 마지막 적재일로부터 며칠 지났는지 계산
        long daysPassed =
                ChronoUnit.DAYS.between(
                        latestCreatedDate,
                        today
                );

        // 마지막 적재 당일 또는 그 이전이면 실행하지 않음
        if (daysPassed <= 0) {
            return;
        }

        // 4. 7일 단위 날짜에서만 실행
        //
        // 예:
        // 7일  -> O
        // 8일  -> X
        // 14일 -> O
        // 15일 -> X
        if (daysPassed % CRAWL_INTERVAL_DAYS != 0) {
            return;
        }

        // 5. RSS 크롤링 실행
        try {
            int savedCount =
                    newsRssCrawlService.crawlAndSave();

            log.info(
                    "주간 뉴스 RSS 크롤링 완료 - 최근 적재일: {}, 경과일: {}일, 신규 기사: {}건",
                    latestCreatedDate,
                    daysPassed,
                    savedCount
            );

        } catch (Exception e) {
            log.error(
                    "주간 뉴스 RSS 크롤링 실패 - 최근 적재일: {}, 경과일: {}일",
                    latestCreatedDate,
                    daysPassed,
                    e
            );
        }
    }
}