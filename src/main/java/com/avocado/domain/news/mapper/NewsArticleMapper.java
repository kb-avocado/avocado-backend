// news/mapper/NewsArticleMapper.java
package com.avocado.domain.news.mapper;

import com.avocado.domain.news.domain.NewsArticle;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.time.LocalDateTime;
import java.util.List;

@MapperScan
public interface NewsArticleMapper {

    // completed:
    // null이면 전체
    // true면 완료된 것만
    // false면 진행중(미완료)인 것만
    List<NewsArticle> findList(
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("childId") Long childId,
            @Param("completed") Boolean completed
    );

    long countAll(
            @Param("childId") Long childId,
            @Param("completed") Boolean completed
    );

    NewsArticle findById(
            @Param("id") Long id
    );

    // =========================
    // RSS 크롤링용
    // =========================

    // 동일 기사 중복 저장 방지
    boolean existsByLink(
            @Param("link") String link
    );

    // 신규 기사 저장
    void insert(
            NewsArticle article
    );

    // 현재 저장된 전체 기사 수
    long countArticles();

    // 가장 최근 뉴스 적재 시각
    LocalDateTime findLatestCreatedAt();

    // =========================
    // 뉴스 조회 권한 검증용
    // =========================

    // 조회 대상 자녀 회원이 존재하는지 확인
    boolean existsChildById(
            @Param("childId") Long childId
    );

    // 보호자와 자녀의 활성 가족 관계가 존재하는지 확인
    boolean existsActiveFamilyRelation(
            @Param("parentId") Long parentId,
            @Param("childId") Long childId
    );
}