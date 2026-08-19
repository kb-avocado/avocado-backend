package com.avocado.domain.family.service;

import com.avocado.domain.family.domain.FamilyRelationStatus;
import com.avocado.domain.family.dto.request.FamilyRequestConfirmRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestDecisionRequestDto;
import com.avocado.domain.family.dto.response.FamilyRequestCheckResponseDto;
import com.avocado.domain.family.dto.response.FamilyRequestResponseDto;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.security.jwt.dto.AuthUser;

import java.util.List;

/**
 * 가족 연결 요청의 생성부터 확정까지를 담당한다.
 * 아이가 요청하고(PENDING), 보호자가 승인하고(APPROVED), 아이가 확정하면(ACTIVE) 연결이 끝난다.
 */
public interface FamilyService {

    /**
     * 아이가 보호자의 초대 코드로 가족 연결을 요청한다.
     *
     * @param authUser 요청한 아이 (토큰에서 꺼낸 인증 주체)
     * @param request  초대 코드
     * @return 만들어진 요청 정보
     * @throws BusinessException 아이 계정이 아니거나, 초대 코드가 없거나, 이미 연결된 경우
     */
    FamilyRequestResponseDto createRequest(
            AuthUser authUser,
            FamilyRequestCreateRequestDto request
    );

    /**
     * 아이가 자기 요청이 어디까지 진행됐는지 확인한다. 대기 화면에서 주기적으로 부른다.
     *
     * @throws BusinessException 요청이 없거나(404) 본인 요청이 아닌 경우(403)
     */
    FamilyRequestResponseDto findForChild(AuthUser authUser, Long requestId);

    /**
     * 보호자가 자기에게 온 요청을 확인한다.
     *
     * @throws BusinessException 요청이 없거나(404) 본인에게 온 요청이 아닌 경우(403)
     */
    FamilyRequestCheckResponseDto findForParent(AuthUser authUser, Long requestId);

    /**
     * 보호자가 자기에게 온 요청을 목록으로 확인한다.
     * 요청은 알림으로만 닿아 알림을 지우면 다시 찾을 길이 없으므로 목록으로도 열어 둔다.
     *
     * @param status 걸러낼 상태. null이면 상태를 가리지 않는다.
     * @throws BusinessException 보호자 계정이 아닌 경우(403)
     */
    List<FamilyRequestCheckResponseDto> findAllForParent(
            AuthUser authUser,
            FamilyRelationStatus status
    );

    /**
     * 보호자가 자기에게 온 요청을 승인하거나 거절한다.
     *
     * @throws BusinessException 요청이 없거나(404), 본인에게 온 요청이 아니거나(403), 이미 처리된 요청인 경우(409)
     */
    FamilyRequestCheckResponseDto decide(
            AuthUser authUser,
            Long requestId,
            FamilyRequestDecisionRequestDto request
    );

    /**
     * 아이가 보호자를 확인하고 연결을 확정하거나, 요청을 취소한다. 가족 연결의 마지막 단계다.
     * 확정하면 관계가 ACTIVE가 되고, 아이 계정도 함께 ACTIVE로 바뀐다.
     * 두 테이블을 함께 바꾸므로 하나라도 실패하면 전부 되돌린다.
     *
     * 확정은 보호자가 승인한(APPROVED) 요청에만 할 수 있지만,
     * 취소는 승인을 기다리는(PENDING) 요청에도 할 수 있다.
     *
     * @throws BusinessException 요청이 없거나(404), 본인 요청이 아니거나(403),
     *                           확정인데 보호자가 아직 승인하지 않았거나(409),
     *                           취소인데 이미 끝난 요청인 경우(409)
     */
    FamilyRequestResponseDto confirm(
            AuthUser authUser,
            Long requestId,
            FamilyRequestConfirmRequestDto request
    );

    /**
     * 부모와 아이가 ACTIVE 가족 관계인지 검증한다.
     *
     * @param parentId 부모 회원 ID
     * @param childId  아이 회원 ID
     */
    void validateActiveRelation(
            Long parentId,
            Long childId
    );
}
