package com.avocado.domain.family.mapper;

import com.avocado.domain.family.domain.FamilyRelation;
import com.avocado.domain.family.domain.FamilyRelationStatus;
import org.apache.ibatis.annotations.Param;

/**
 * 가족 연결 요청의 생성과 상태 변경을 담당한다.
 * 로그인 응답에 쓰이는 가족 관계 조회는 UserMapper에 이미 있어 이곳에 두지 않는다.
 */
public interface FamilyRelationMapper {

    // 이미 보호자와 연결된 아이인지 확인 (아이 하나에 보호자 하나)
    boolean existsActiveByChildId(
            @Param("childId") Long childId
    );

    // 아이가 새 요청을 만들 때, 진행 중이던 이전 요청을 모두 정리한다.
    int cancelInProgressByChildId(
            @Param("childId") Long childId
    );

    // (보호자, 아이)는 UNIQUE라 최대 한 건. 거절·취소 이력이 남아 있는지 확인할 때 사용.
    FamilyRelation selectByParentIdAndChildId(
            @Param("parentId") Long parentId,
            @Param("childId") Long childId
    );

    // 요청 생성. 성공하면 인자로 넘긴 relation의 id에 생성된 PK가 채워진다.
    void insertRequest(
            FamilyRelation relation
    );

    // 폴링·보호자 확인·당사자 검증에 공통으로 쓰는 조회. 보호자와 아이 이름까지 채워 준다.
    FamilyRelation selectDetailById(
            @Param("id") Long id
    );

    /**
     * 지금 상태가 fromStatus일 때만 toStatus로 바꾼다.
     *
     * @return 바뀐 행 수. 0이면 이미 다른 상태로 처리된 요청이다.
     */
    int updateStatus(
            @Param("id") Long id,
            @Param("fromStatus") FamilyRelationStatus fromStatus,
            @Param("toStatus") FamilyRelationStatus toStatus
    );

    // 부모와 아이 사이 ACTIVE 가족관계가 존재 확인.
    boolean existsActiveRelation(
            @Param("parentId") Long parentId,
            @Param("childId") Long childId
    );
}
