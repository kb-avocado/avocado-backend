package com.avocado.news.batch;

import com.avocado.news.domain.NewsArticle;
import com.avocado.news.mapper.NewsArticleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsRssCrawlService {

    private static final String RSS_URL = "https://www.econoi.com/rss/allArticle.xml";

    private static final DateTimeFormatter PUB_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final int SUBTITLE_MAX_LENGTH = 150;

    // 기사별 오늘의 챌린지. DB insert 시 이 중 하나를 랜덤으로 골라 고정 저장한다.
    // 저장 이후에는 한 기사 당 챌린지 고정
    private static final List<String> CHALLENGE_TEMPLATES = List.of(
    "가장 기억에 남는 문장이나 내용을 하나 골라, 이유를 설명해 보세요.",
    "이 글을 한 문장으로 요약해 보세요.",
    "이 글을 읽고 새롭게 알게 된 점을 1가지 적어 보세요.",
    "글에서 가장 중요한 단어 3~5개를 골라 보세요.",
    "글을 읽고 떠오른 나의 경험이나 주변 사례를 이야기해 보세요.",
    "이 글에 어울리는 이모지를 3개 골라보세요.",
    "이 글을 읽고 가장 궁금한 점을 하나 적어 보세요.",
    "글 속 어휘를 활용해 가족 토론 주제를 만들어보세요.",
    "글 속 핵심 어휘 5개를 주제별로 분류해 보세요. (예: 경제/환경/사회 등)"
    );

    private final NewsArticleMapper newsArticleMapper;

    /**
     * RSS 피드를 읽어 아직 저장되지 않은(link 기준 신규) 기사만 DB에 저장.
     * @return 새로 저장된 기사 수
     */
    @Transactional
    public int crawlAndSave() {
        List<RssItem> items = fetchItems();
        int savedCount = 0;

        for (RssItem item : items) {
            if (newsArticleMapper.existsByLink(item.link())) {
                continue; // 이미 저장된 기사는 건너뜀
            }

            NewsArticle article = toNewsArticle(item);
            newsArticleMapper.insert(article);
            savedCount++;
        }

        log.info("RSS 크롤링 완료 - 전체 {}건 중 신규 {}건 저장", items.size(), savedCount);
        return savedCount;
    }

    private List<RssItem> fetchItems() {
        try (InputStream is = new URL(RSS_URL).openStream()) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(is);

            NodeList itemNodes = doc.getElementsByTagName("item");
            List<RssItem> items = new ArrayList<>();

            for (int i = 0; i < itemNodes.getLength(); i++) {
                Element itemEl = (Element) itemNodes.item(i);
                items.add(new RssItem(
                        text(itemEl, "title"),
                        text(itemEl, "link"),
                        text(itemEl, "description"),
                        text(itemEl, "pubDate")
                ));
            }

            return items;
        } catch (Exception e) {
            log.error("RSS 피드 조회 실패", e);
            throw new RuntimeException("RSS 피드를 불러오는 데 실패했습니다.", e);
        }
    }

    private String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        if (nodes.getLength() == 0) return null;
        return nodes.item(0).getTextContent().trim();
    }

    private NewsArticle toNewsArticle(RssItem item) {
        NewsArticle article = new NewsArticle();
        article.setTitle(item.title());
        article.setSubtitle(truncate(item.description()));
        article.setLink(item.link());
        article.setPublishedAt(parsePubDate(item.pubDate()));
        article.setCreatedAt(LocalDateTime.now());
        // 신규 기사가 DB에 처음 저장되는 이 시점에만 랜덤으로 뽑고, 이후로는 고정.
        article.setTodayChallenge(pickRandomChallenge());
        return article;
    }

    private LocalDateTime parsePubDate(String pubDateRaw) {
        if (pubDateRaw == null || pubDateRaw.isBlank()) {
            log.warn("pubDate가 비어있어 현재 시각으로 대체");
            return LocalDateTime.now();
        }
        try {
            return LocalDateTime.parse(pubDateRaw, PUB_DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("pubDate 파싱 실패, 현재 시각으로 대체 - 원본값: {}", pubDateRaw);
            return LocalDateTime.now();
        }
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > SUBTITLE_MAX_LENGTH
                ? text.substring(0, SUBTITLE_MAX_LENGTH) + "..."
                : text;
    }

    private String pickRandomChallenge() {
        int index = ThreadLocalRandom.current().nextInt(CHALLENGE_TEMPLATES.size());
        return CHALLENGE_TEMPLATES.get(index);
    }

    private record RssItem(String title, String link, String description, String pubDate) {}
}