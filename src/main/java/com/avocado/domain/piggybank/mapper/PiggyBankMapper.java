package com.avocado.domain.piggybank.mapper;

import com.avocado.domain.piggybank.domain.PiggyBankBonusType;
import com.avocado.domain.piggybank.domain.PiggyBank;
import com.avocado.domain.piggybank.domain.PiggyBankRefundTarget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PiggyBankMapper {

    PiggyBank selectById(@Param("id") Long id);

    int updateBonus(
            @Param("id") Long id,
            @Param("piggyBankBonusType") PiggyBankBonusType piggyBankBonusType,
            @Param("bonusValue") Long bonusValue
    );

    int increaseBalance(
            @Param("id") Long id,
            @Param("balance") Long balance,
            @Param("status") String status,
            @Param("firstDepositedAt") LocalDateTime firstDepositedAt,
            @Param("targetReachedAt") LocalDateTime targetReachedAt
    );

    // 지갑 + 상태(여러 개)로 저금통 목록 조회 (진행중/완료 탭 공용)
    List<PiggyBank> selectByWalletIdAndStatuses(
            @Param("walletId") Long walletId,
            @Param("statuses") List<String> statuses
    );
    //지갑 + 상태(여러 개)에 해당하는 저금통 개수 (진행중 개수 카운트용)
    int countByWalletIdAndStatuses(
            @Param("walletId") Long walletId,
            @Param("statuses") List<String> statuses
    );

    //저금통 생성 추가
    int insert(PiggyBank piggyBank);

    Long selectLastInsertId();

    //저금통 삭제 추가
    int cancel(@Param("id") Long id);

    //저금통 보너스 지급 완료 처리
    int markBonusPaid(
            @Param("id") Long id,
            @Param("bonusPaidAt") LocalDateTime bonusPaidAt
    );

    // 목표 도달 후 7일 경과, 승격 대상 저금통 조회 (환급에 필요한 childId/balance 포함)
    List<PiggyBankRefundTarget> selectPromotable();

    // 특정 저금통 하나를 ACHIEVE로 승격
    int promoteById(@Param("id") Long id);

    // 같은 지갑의 기존 즐겨찾기 모두 해제 (1개 보장용)
    int clearFavoritesByWallet(@Param("walletId") Long walletId);

    // 특정 저금통 즐겨찾기 on/off
    int updateFavorite(@Param("id") Long id, @Param("favorite") boolean favorite);


    // 상태/즐겨찾기 여부와 무관하게, 이 지갑에 저금통이 하나라도 있는지 (홈 화면 빈 상태 문구 분기용)
    boolean existsByWalletId(@Param("walletId") Long walletId);
    // 환급: 저금통 잔액 0 처리
    int zeroBalance(@Param("id") Long id);
}