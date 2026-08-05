// news/mapper/NewsActivityMapper.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NewsActivityMapper {
    NewsActivity findByChildIdAndArticleId(@Param("childId") Long childId, @Param("articleId") Long articleId);

    List<NewsActivity> findByChildIdAndArticleIds(
            @Param("childId") Long childId,
            @Param("articleIds") List<Long> articleIds
    );
    void insert(NewsActivity activity);
    void update(NewsActivity activity);
}