// news/mapper/NewsArticleMapper.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsArticle;
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
}