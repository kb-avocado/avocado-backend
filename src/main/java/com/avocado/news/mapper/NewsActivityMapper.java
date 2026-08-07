// news/mapper/NewsActivityMapper.java
package com.avocado.news.mapper;

import com.avocado.news.domain.NewsActivity;
import org.apache.ibatis.annotations.Param;
import org.mybatis.spring.annotation.MapperScan;

import java.util.List;

@MapperScan
public interface NewsActivityMapper {
    NewsActivity findByChildIdAndArticleId(@Param("childId") Long childId, @Param("articleId") Long articleId);

    List<NewsActivity> findByChildIdAndArticleIds(
            @Param("childId") Long childId,
            @Param("articleIds") List<Long> articleIds
    );
    void insert(NewsActivity activity);
    void update(NewsActivity activity);
}