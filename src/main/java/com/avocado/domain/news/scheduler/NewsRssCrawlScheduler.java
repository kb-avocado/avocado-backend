package com.avocado.domain.news.scheduler;

import com.avocado.domain.news.batch.NewsRssCrawlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsRssCrawlScheduler {

    private final NewsRssCrawlService newsRssCrawlService;

    /**
     * 매일 오전 6시에 RSS를 조회한다.
     *
     * 기존 기사는 link 기준으로 중복 체크하고,
     * 새로 올라온 기사만 DB에 저장한다.
     */
    @Scheduled(
            cron = "0 0 6 * * *",
            zone = "Asia/Seoul"
    )
    public void crawlDailyNews() {
        try {
            int savedCount = newsRssCrawlService.crawlAndSave();

            log.info(
                    "일일 뉴스 RSS 크롤링 완료 - 신규 기사 {}건 저장",
                    savedCount
            );
        } catch (Exception e) {
            log.error(
                    "일일 뉴스 RSS 크롤링 실패",
                    e
            );
        }
    }
}