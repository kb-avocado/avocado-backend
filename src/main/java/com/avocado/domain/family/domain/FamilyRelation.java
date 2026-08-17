package com.avocado.domain.family.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// family_relations 테이블 매핑 도메인
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FamilyRelation {

    private Long id;
    private Long parentId;
    private Long childId;
    private FamilyRelationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 아래 두 필드는 users를 조인해야 채워진다. 조인하지 않는 조회에서는 null이다.
    private String parentName;
    private String childName;
}
