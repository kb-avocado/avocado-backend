// news/mapper/NewsArticleMapper.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsArticleMapper {
    List<NewsArticle> findList(@Param("offset") int offset, @Param("limit") int limit);
    long countAll();
    NewsArticle findById(@Param("id") Long id);
}