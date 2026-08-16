package com.avocado.domain.merchant.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class MerchantVo {

    private Long id;

    private String name;

    private String businessNo;

    private String address;

    private String category;

    private Boolean restrictedForChild;

    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isRestrictedForChild() {
        return Boolean.TRUE.equals(restrictedForChild);
    }
}
