// news/mapper/NewsArticleMapper.java
package com.avocado.domain.news.mapper;

import com.avocado.domain.news.domain.NewsArticle;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

@MapperScan
public interface NewsArticleMapper {
    List<NewsArticle> findList(@Param("offset") int offset, @Param("limit") int limit);
    long countAll();
    NewsArticle findById(@Param("id") Long id);

    // RSS 크롤링용
    boolean existsByLink(@Param("link") String link);
    void insert(NewsArticle article);

    // 조회 대상 자녀 회원이 존재하는지 확인
    boolean existsChildById(@Param("childId") Long childId);

    // 보호자와 자녀의 활성 가족 관계가 존재하는지 확인
    boolean existsActiveFamilyRelation(
            @Param("parentId") Long parentId,
            @Param("childId") Long childId
    );
}