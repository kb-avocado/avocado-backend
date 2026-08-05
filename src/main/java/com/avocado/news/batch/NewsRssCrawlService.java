package com.avocado.news.batch;

import com.avocado.news.domain.NewsArticle;
import com.avocado.news.mapper.NewsArticleMapper;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRssCrawlService {

    private static final String RSS_URL = "https://www.econoi.com/rss/S1N3.xml";
    // TODO: 실제 어린이 경제신문 카테고리별 RSS 주소 확인 필요.
    // 지금 URL은 스크린샷 기준 추정치라, 실제 RSS XML이 응답되는 정확한 엔드포인트로 교체해야 함

    private final NewsArticleMapper newsArticleMapper;

    /**
     * RSS 피드를 읽어 아직 저장되지 않은(link 기준 신규) 기사만 DB에 저장.
     * @return 새로 저장된 기사 수
     */
    @Transactional
    public int crawlAndSave() {
        List<SyndEntry> entries = fetchFeed();
        int savedCount = 0;

        for (SyndEntry entry : entries) {
            if (newsArticleMapper.existsByLink(entry.getLink())) {
                continue; // 이미 저장된 기사는 건너뜀
            }

            NewsArticle article = toNewsArticle(entry);
            newsArticleMapper.insert(article);
            savedCount++;
        }

        log.info("RSS 크롤링 완료 - 전체 {}건 중 신규 {}건 저장", entries.size(), savedCount);
        return savedCount;
    }

    private List<SyndEntry> fetchFeed() {
        try (XmlReader reader = new XmlReader(new URL(RSS_URL))) {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(reader);
            return feed.getEntries();
        } catch (Exception e) {
            log.error("RSS 피드 조회 실패", e);
            throw new RuntimeException("RSS 피드를 불러오는 데 실패했습니다.", e);
        }
    }

    private NewsArticle toNewsArticle(SyndEntry entry) {
        NewsArticle article = new NewsArticle();
        article.setTitle(entry.getTitle());
        article.setSubtitle(extractSubtitle(entry));
        article.setLink(entry.getLink());
        article.setPublishedAt(toLocalDateTime(entry.getPublishedDate()));
        article.setCreatedAt(LocalDateTime.now());
        // challengeQuestion은 RSS에 없는 필드라 null로 저장.
        // TODO: 어드민 화면에서 기사별로 챌린지 질문을 나중에 직접 입력하는 방식이 필요할지 확인 필요
        return article;
    }

    private String extractSubtitle(SyndEntry entry) {
        if (entry.getDescription() == null) return null;
        String raw = entry.getDescription().getValue();
        // 요약이 너무 길면 일부만 잘라서 subtitle로 사용
        return raw.length() > 150 ? raw.substring(0, 150) + "..." : raw;
    }

    private LocalDateTime toLocalDateTime(java.util.Date date) {
        if (date == null) return LocalDateTime.now();
        return date.toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime();
    }
}