package com.avocado.domain.news.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.news.domain.NewsActivity;
import com.avocado.domain.news.domain.NewsArticle;
import com.avocado.domain.news.dto.request.NewsAnswerRequestDto;
import com.avocado.domain.news.dto.response.NewsAnswerResponseDto;
import com.avocado.domain.news.dto.response.NewsDetailResponseDto;
import com.avocado.domain.news.dto.response.NewsListItemDto;
import com.avocado.domain.news.dto.response.NewsListResponseDto;
import com.avocado.domain.news.mapper.NewsActivityMapper;
import com.avocado.domain.news.mapper.NewsArticleMapper;
import com.avocado.domain.news.mapper.NewsConverter;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsServiceImpl implements NewsService {

    private final NewsArticleMapper newsArticleMapper;
    private final NewsActivityMapper newsActivityMapper;
    private final NewsConverter newsConverter;
    private final NotificationService notificationService;
    private final UserMapper userMapper;

    @Override
    public NewsListResponseDto getNewsList(int page, int size, Long childId, Boolean completed, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        int offset = page * size;
        List<NewsArticle> articles = newsArticleMapper.findList(offset, size, targetChildId, completed);
        long totalCount = newsArticleMapper.countAll(targetChildId, completed);

        List<Long> articleIds = articles.stream()
                .map(NewsArticle::getId)
                .toList();

        Map<Long, NewsActivity> activityByArticleId = articleIds.isEmpty()
                ? Collections.emptyMap()
                : newsActivityMapper.findByChildIdAndArticleIds(targetChildId, articleIds).stream()
                .collect(Collectors.toMap(NewsActivity::getArticleId, Function.identity()));

        List<NewsListItemDto> items = articles.stream()
                .map(article -> newsConverter.toListItemDto(
                        article,
                        activityByArticleId.get(article.getId())
                ))
                .toList();

        return NewsListResponseDto.builder()
                .totalCount(totalCount)
                .news(items)
                .build();
    }

    @Override
    @Transactional
    public NewsDetailResponseDto getNewsDetail(Long newsId, Long childId, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        NewsArticle article = newsArticleMapper.findById(newsId);
        if (article == null) {
            throw new BusinessException(ErrorCode.NEWS_NOT_FOUND);
        }

        NewsActivity activity = newsActivityMapper.findByChildIdAndArticleId(targetChildId, newsId);

        if (activity == null) {
            activity = NewsActivity.createViewed(targetChildId, newsId);
            newsActivityMapper.insert(activity);
        } else if (activity.getViewedAt() == null) {
            activity.markViewed();
            newsActivityMapper.update(activity);
        }

        return newsConverter.toDetailDto(article, activity);
    }

    @Override
    @Transactional
    public NewsAnswerResponseDto saveAnswer(Long newsId, Long childId, AuthUser authUser, NewsAnswerRequestDto request) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        NewsActivity activity = newsActivityMapper.findByChildIdAndArticleId(targetChildId, newsId);

        if (activity == null) {
            activity = NewsActivity.createViewed(targetChildId, newsId);
            activity.saveAnswer(request.getChildAnswer());
            newsActivityMapper.insert(activity);
        } else {
            activity.saveAnswer(request.getChildAnswer());
            newsActivityMapper.update(activity);
        }

        Long parentId = userMapper.selectParentIdByChildId(targetChildId);
        if (parentId != null) {
            NewsArticle article = newsArticleMapper.findById(newsId);
            String title = (article != null) ? article.getTitle() : "뉴스";
            notificationService.create(
                    parentId,
                    NotificationType.NEWS_ACTIVITY_COMPLETED,
                    String.format("자녀가 '%s' 뉴스 활동을 완료했어요.", title)
            );
        }

        return newsConverter.toAnswerResponseDto(activity);
    }

    /*
     * 요청받은 childId와 로그인 사용자 정보를 바탕으로
     * 실제 조회/기록 대상 childId를 결정하고 접근 권한을 검증한다.
     * - childId가 없으면: 로그인 사용자가 CHILD일 때만 본인 ID로 대체한다. (PARENT는 필수)
     * - childId가 있으면: 본인(CHILD) 또는 연결된 보호자(PARENT)인지 검증한다.
     */
    private Long resolveTargetChildId(Long childId, AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }

        Long targetChildId = childId;
        if (targetChildId == null) {
            if (!UserType.CHILD.equals(authUser.getUserType())) {
                throw new BusinessException(INVALID_REQUEST);
            }
            targetChildId = authUser.getUserId();
        }

        validateChildAccess(targetChildId, authUser);
        return targetChildId;
    }

    private void validateChildAccess(Long childId, AuthUser authUser) {
        if (!newsArticleMapper.existsChildById(childId)) {
            throw new BusinessException(CHILD_NOT_FOUND);
        }

        if (isChildOwner(childId, authUser)) {
            return;
        }

        if (isConnectedParent(childId, authUser)) {
            return;
        }

        throw new BusinessException(FORBIDDEN);
    }

    /*
     * 로그인 사용자가 CHILD 유형이고,
     * 본인의 childId로 접근하는 경우인지 확인한다.
     */
    private boolean isChildOwner(Long childId, AuthUser authUser) {
        return UserType.CHILD.equals(authUser.getUserType())
                && childId.equals(authUser.getUserId());
    }

    /*
     * 로그인 사용자가 PARENT 유형이고,
     * 대상 자녀와 ACTIVE 가족 관계가 있는지 확인한다.
     */
    private boolean isConnectedParent(Long childId, AuthUser authUser) {
        return UserType.PARENT.equals(authUser.getUserType())
                && newsArticleMapper.existsActiveFamilyRelation(
                authUser.getUserId(),
                childId
        );
    }
}