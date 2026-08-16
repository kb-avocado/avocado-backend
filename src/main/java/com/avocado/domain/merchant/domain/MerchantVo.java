package com.avocado.domain.merchant.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 결제 가맹점 정보를 담는 도메인 값 객체.
 * POS 결제 시 가맹점 존재 여부, 영업 상태, 아이 결제 제한 여부를 판단하는 기준 데이터로 사용된다.
 */
@Getter
@Setter
@NoArgsConstructor
public class MerchantVo {

    // 가맹점 고유 ID
    private Long id;

    // 사용자에게 표시되는 가맹점명
    private String name;

    // 사업자등록번호
    private String businessNo;

    // 가맹점 주소
    private String address;

    // 소비 리포트와 결제 내역 분류에 사용하는 업종 카테고리
    private String category;

    // 아이 선불지갑 결제를 제한해야 하는 가맹점인지 여부
    private Boolean restrictedForChild;

    // 가맹점 운영 상태. ACTIVE인 경우에만 결제 가능 대상으로 본다.
    private String status;

    // 가맹점 등록 시각
    private LocalDateTime createdAt;

    // 가맹점 정보 수정 시각
    private LocalDateTime updatedAt;

    /**
     * 결제 가능한 활성 가맹점인지 확인한다.
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * 아이 선불지갑으로 결제할 수 없는 제한 가맹점인지 확인한다.
     */
    public boolean isRestrictedForChild() {
        return Boolean.TRUE.equals(restrictedForChild);
    }
}
