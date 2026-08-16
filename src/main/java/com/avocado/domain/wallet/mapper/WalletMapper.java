package com.avocado.domain.wallet.mapper;

import com.avocado.domain.wallet.domain.WalletVo;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

/**
 * 선불지갑 도메인의 데이터 접근을 담당한다.
 *
 * 지갑 조회, 소유 관계 확인, 활성 상태 확인,
 * 잔액 변경 및 동시성 제어를 위한 잠금 조회 기능을 제공한다.
 */
public interface WalletMapper {

    /**
     * 사용자 ID로 등록된 선불지갑 ID를 조회한다.
     *
     * @param userId 조회할 사용자 ID
     * @return 사용자가 보유한 선불지갑 ID.
     *         지갑이 존재하지 않으면 Optional.empty()
     */
    Optional<Long> findWalletIdByUserId(
            @Param("userId") Long userId
    );

    /**
     * 자녀 회원 ID로 선불지갑 단건 정보를 조회한다.
     *
     * 지갑 상태와 관계없이 해당 자녀가 보유한 선불지갑 정보를 조회한다.
     *
     * @param childId 조회할 자녀 회원 ID
     * @return 자녀의 선불지갑 정보.
     *         지갑이 존재하지 않으면 Optional.empty()
     */
    Optional<WalletVo> findByChildId(
            @Param("childId") Long childId
    );

    /**
     * 아이 회원이 선불지갑을 보유하고 있는지 확인한다.
     *
     * @param childId 확인할 아이 회원 ID
     * @return 선불지갑이 존재하면 true
     */
    boolean existsByChildId(
            @Param("childId") Long childId
    );

    /**
     * 동일한 지갑 계좌번호가 존재하는지 확인한다.
     *
     * @param walletNumber 확인할 지갑 계좌번호
     * @return 동일한 지갑 계좌번호가 존재하면 true
     */
    boolean existsByWalletNumber(
            @Param("walletNumber") String walletNumber
    );

    /**
     * 해당 선불지갑이 자녀 소유이며 ACTIVE 상태인지 확인한다.
     *
     * 지갑 ID와 자녀 ID가 일치하고,
     * 지갑 상태가 ACTIVE인 경우에만 true를 반환한다.
     *
     * @param walletId 확인할 선불지갑 ID
     * @param childId 선불지갑 소유 자녀 ID
     * @return 해당 자녀 소유의 ACTIVE 선불지갑이면 true
     */
    boolean existsActiveByIdAndChildId(
            @Param("childId") Long childId,
            @Param("walletId") Long walletId
    );

    /**
     * 아이 회원의 선불지갑을 생성한다.
     *
     * @param wallet 생성할 선불지갑 정보
     * @return 생성된 행 수
     */
    int insert(
            WalletVo wallet
    );

    /**
     * 자녀 회원 ID로 ACTIVE 상태의 선불지갑을 조회한다.
     *
     * @param childId 조회할 자녀 회원 ID
     * @return ACTIVE 상태의 선불지갑 정보
     */
    Optional<WalletVo> findActiveByChildId(
            @Param("childId") Long childId
    );

    /**
     * 선불지갑 잔액을 지정한 금액만큼 증가시킨다.
     *
     * ACTIVE 상태의 선불지갑만 잔액을 변경한다.
     *
     * @param walletId 잔액을 증가시킬 선불지갑 ID
     * @param amount 증가시킬 금액
     * @return 변경된 행 수. 정상적으로 변경되면 1
     */
    int increaseBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Long amount
    );

    /**
     * 선불지갑 잔액을 지정한 금액만큼 감소시킨다.
     *
     * ACTIVE 상태이며 현재 잔액이 출금 금액 이상인 경우에만
     * 잔액을 감소시킨다.
     *
     * @param walletId 잔액을 감소시킬 선불지갑 ID
     * @param amount 감소시킬 금액
     * @return 변경된 행 수.
     *         정상적으로 출금되면 1,
     *         잔액 부족 또는 사용할 수 없는 지갑이면 0
     */
    int decreaseBalance(
            @Param("walletId") Long walletId,
            @Param("amount") Long amount
    );

    /**
     * 해당 자녀가 소유한 선불지갑을 배타적 잠금으로 조회한다.
     *
     * 송금, 결제 등 잔액 변경이 발생하는 트랜잭션에서
     * 동일한 지갑에 대한 동시 수정으로 인해
     * 잔액 정합성이 깨지는 것을 방지하기 위해 사용한다.
     *
     * SQL에서는 SELECT ... FOR UPDATE를 사용한다.
     *
     * @param walletId 잠금 조회할 선불지갑 ID
     * @param childId 선불지갑 소유 자녀 ID
     * @return 해당 자녀 소유의 선불지갑 정보.
     *         조건에 맞는 지갑이 없으면 Optional.empty()
     */
    Optional<WalletVo> findForUpdateByChildId(
            @Param("childId") Long childId
    );
}
