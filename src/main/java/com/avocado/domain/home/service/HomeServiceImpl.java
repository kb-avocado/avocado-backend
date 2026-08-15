package com.avocado.domain.home.service;

import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.home.dto.response.HomeFavoritePiggyBankDto;
import com.avocado.domain.home.dto.response.HomeResponseDto;
import com.avocado.domain.news.dto.response.NewsListItemDto;
import com.avocado.domain.news.service.NewsService;
import com.avocado.domain.piggybank.domain.PiggyBank;
import com.avocado.domain.piggybank.mapper.PiggyBankMapper;
import com.avocado.domain.report.mapper.ReportMapper;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeServiceImpl implements HomeService {

    // 진행 중인 저금통(즐겨찾기 후보)로 볼 상태값들
    private static final List<String> IN_PROGRESS_STATUSES = List.of("ACTIVE", "PENDING_ACHIEVE");
    // 홈에는 최근 신문 2건만 노출
    private static final int NEWS_PREVIEW_SIZE = 2;

    private final UserMapper userMapper;
    private final FamilyRelationMapper familyRelationMapper;
    private final WalletMapper walletMapper;
    private final PiggyBankMapper piggyBankMapper;
    private final ReportMapper reportMapper;
    private final NewsService newsService;

    @Override
    public HomeResponseDto getHome(Long childId, AuthUser authUser) {
        Long targetChildId = resolveTargetChildId(childId, authUser);

        // 아직 선불지갑이 없는 아이(가입 직후 등)일 수 있으므로, 지갑이 없으면
        // 저금통/리포트는 빈 값으로 채우고 에러로 취급하지 않는다. (지갑 자체는 /api/wallets에서 별도 조회)
        Long walletId = walletMapper.findWalletIdByUserId(targetChildId).orElse(null);

        List<HomeFavoritePiggyBankDto> favoritePiggyBanks = walletId == null
                ? Collections.emptyList()
                : getFavoritePiggyBanks(walletId);

        Long todaySpent = walletId == null ? 0L : reportMapper.sumSpentAmountByDate(walletId, LocalDate.now());
        Long monthSpent = walletId == null ? 0L : reportMapper.sumSpentAmount(walletId, currentYearMonth());

        List<NewsListItemDto> news = newsService
                .getNewsList(0, NEWS_PREVIEW_SIZE, targetChildId, authUser)
                .getNews();

        return HomeResponseDto.builder()
                .favoritePiggyBanks(favoritePiggyBanks)
                .todaySpent(todaySpent)
                .monthSpent(monthSpent)
                .news(news)
                .build();
    }

    private List<HomeFavoritePiggyBankDto> getFavoritePiggyBanks(Long walletId) {
        return piggyBankMapper.selectByWalletIdAndStatuses(walletId, IN_PROGRESS_STATUSES).stream()
                .filter(piggyBank -> Boolean.TRUE.equals(piggyBank.getIsFavorite()))
                .map(this::toFavoriteDto)
                .toList();
    }

    private HomeFavoritePiggyBankDto toFavoriteDto(PiggyBank piggyBank) {
        long target = piggyBank.getTargetAmount() == null ? 0 : piggyBank.getTargetAmount();
        long saved = piggyBank.getBalance() == null ? 0 : piggyBank.getBalance();
        int rate = target == 0 ? 0 : (int) (saved * 100 / target);

        return HomeFavoritePiggyBankDto.builder()
                .piggyBankId(piggyBank.getId())
                .name(piggyBank.getName())
                .icon(piggyBank.getIcon())
                .balance(piggyBank.getBalance())
                .targetAmount(piggyBank.getTargetAmount())
                .progressRate(rate)
                .build();
    }

    private String currentYearMonth() {
        return YearMonth.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    /*
     * 요청받은 childId와 로그인 사용자 정보를 바탕으로
     * 실제 조회 대상 childId를 결정하고 접근 권한을 검증한다.
     * News 도메인의 resolveTargetChildId와 동일한 패턴이다.
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
        if (!userMapper.existsChildById(childId)) {
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

    private boolean isChildOwner(Long childId, AuthUser authUser) {
        return UserType.CHILD.equals(authUser.getUserType())
                && childId.equals(authUser.getUserId());
    }

    private boolean isConnectedParent(Long childId, AuthUser authUser) {
        return UserType.PARENT.equals(authUser.getUserType())
                && familyRelationMapper.existsActiveRelation(authUser.getUserId(), childId);
    }
}