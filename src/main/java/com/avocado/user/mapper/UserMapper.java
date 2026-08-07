package com.avocado.user.mapper;

import com.avocado.user.domain.User;
import com.avocado.user.dto.response.LoginChildDto;
import com.avocado.user.dto.response.LoginFamilyDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 로그인 응답을 한 번에 구성하기 위해 accounts, wallets, family_relations 조회도 이곳에 둔다.
 * 각 도메인 매퍼는 다른 담당자의 작업 범위라 수정하지 않는다.
 */
public interface UserMapper {

    // 로그인 시 이메일로 회원 조회 (email은 UNIQUE)
    User selectByEmail(@Param("email") String email);

    // [PARENT] 연동 계좌 ID. 계좌가 여러 개면 먼저 연동한 ACTIVE 계좌를 사용한다.
    Long selectAccountIdByParentId(@Param("parentId") Long parentId);

    // [PARENT] 가족 관계가 ACTIVE인 아이 목록
    List<LoginChildDto> selectChildrenByParentId(@Param("parentId") Long parentId);

    // [CHILD] 선불지갑 ID (지갑 상태와 무관하게 조회)
    Long selectWalletIdByChildId(@Param("childId") Long childId);

    // [CHILD] 연결된 부모 회원 ID
    Long selectParentIdByChildId(@Param("childId") Long childId);

    // [CHILD] 가족 연결 요청 정보 (PENDING 응답용)
    LoginFamilyDto selectFamilyByChildId(@Param("childId") Long childId);

    // 회원가입 시 이메일 중복 검사 (email은 UNIQUE)
    boolean existsByEmail(@Param("email") String email);

    // 회원가입 시 전화번호 중복 검사 (phone은 UNIQUE)
    boolean existsByPhone(@Param("phone") String phone);

    // [PARENT] 초대 코드 중복 검사 (invite_code는 UNIQUE)
    boolean existsByInviteCode(@Param("inviteCode") String inviteCode);

    // 회원가입
    void insertUser(User user);

    // 가족 연결 요청 시 초대 코드로 보호자를 찾는다. (invite_code는 UNIQUE)
    User selectByInviteCode(@Param("inviteCode") String inviteCode);
}
