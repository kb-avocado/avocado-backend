// news/mapper/NewsActivityMapper.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface NewsActivityMapper {
    NewsActivity findByChildIdAndArticleId(@Param("childId") Long childId, @Param("articleId") Long articleId);
    void insert(NewsActivity activity);
    void update(NewsActivity activity);
}