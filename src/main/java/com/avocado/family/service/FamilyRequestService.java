package com.avocado.family.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.response.code.ErrorCode;
import com.avocado.family.domain.FamilyRelation;
import com.avocado.family.domain.FamilyRelationStatus;
import com.avocado.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.family.dto.response.FamilyRequestCheckResponseDto;
import com.avocado.family.dto.response.FamilyRequestResponseDto;
import com.avocado.family.mapper.FamilyRelationMapper;
import com.avocado.jwt.dto.AuthUser;
import com.avocado.user.domain.User;
import com.avocado.user.domain.UserStatus;
import com.avocado.user.domain.UserType;
import com.avocado.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class FamilyRequestService {

    private final FamilyRelationMapper familyRelationMapper;
    private final UserMapper userMapper;

    /**
     * 아이가 보호자의 초대 코드로 가족 연결을 요청한다.
     *
     * @param requester 요청한 아이 (토큰에서 꺼낸 인증 주체)
     * @param request   초대 코드
     * @return 만들어진 요청 정보
     * @throws BusinessException 아이 계정이 아니거나, 초대 코드가 없거나, 이미 연결된 경우
     */
    @Transactional
    public FamilyRequestResponseDto createRequest(
            AuthUser requester,
            FamilyRequestCreateRequestDto request
    ) {
        requireAuthenticated(requester);

        if (requester.getUserType() != UserType.CHILD) {
            throw new BusinessException(ErrorCode.CHILD_ONLY_FAMILY_REQUEST);
        }

        Long childId = requester.getUserId();
        User parent = findParentByInviteCode(request.getCode());

        // 아이 하나에 보호자 하나. 이미 연결됐으면 새 요청을 만들지 않는다.
        if (familyRelationMapper.existsActiveByChildId(childId)) {
            throw new BusinessException(ErrorCode.ALREADY_CONNECTED);
        }

        // 새 코드를 넣었다는 것은 이전 요청을 포기했다는 뜻이다.
        familyRelationMapper.cancelInProgressByChildId(childId);

        Long requestId = createOrRevive(parent.getId(), childId);

        return FamilyRequestResponseDto.builder()
                .requestId(requestId)
                .status(FamilyRelationStatus.PENDING)
                .parentName(parent.getName())
                .build();
    }

    /**
     * 아이가 자기 요청이 어디까지 진행됐는지 확인한다. 대기 화면에서 주기적으로 부른다.
     *
     * @throws BusinessException 요청이 없거나(404) 본인 요청이 아닌 경우(403)
     */
    @Transactional(readOnly = true)
    public FamilyRequestResponseDto findForChild(AuthUser requester, Long requestId) {
        requireAuthenticated(requester);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(requester, relation.getChildId());

        return FamilyRequestResponseDto.builder()
                .requestId(relation.getId())
                .status(relation.getStatus())
                .parentName(relation.getParentName())
                .build();
    }

    /**
     * 보호자가 자기에게 온 요청을 확인한다.
     *
     * @throws BusinessException 요청이 없거나(404) 본인에게 온 요청이 아닌 경우(403)
     */
    @Transactional(readOnly = true)
    public FamilyRequestCheckResponseDto findForParent(AuthUser requester, Long requestId) {
        requireAuthenticated(requester);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(requester, relation.getParentId());

        return FamilyRequestCheckResponseDto.builder()
                .requestId(relation.getId())
                .status(relation.getStatus())
                .childName(relation.getChildName())
                .createdAt(relation.getCreatedAt())
                .build();
    }

    private void requireAuthenticated(AuthUser requester) {
        if (requester == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }
    }

    private FamilyRelation findRelation(Long requestId) {
        FamilyRelation relation = familyRelationMapper.selectDetailById(requestId);

        if (relation == null) {
            throw new BusinessException(ErrorCode.FAMILY_REQUEST_NOT_FOUND);
        }

        return relation;
    }

    /**
     * 요청 ID 숫자만 바꾸면 남의 가족 요청에 닿을 수 있다.
     * 조회든 처리든 당사자인지 반드시 확인한다.
     */
    private void requireOwner(AuthUser requester, Long ownerId) {
        if (!requester.getUserId().equals(ownerId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }
    }

    /**
     * 초대 코드의 주인을 찾는다.
     */
    private User findParentByInviteCode(String code) {
        String inviteCode = code.trim().toUpperCase(Locale.ROOT);
        User parent = userMapper.selectByInviteCode(inviteCode);

        boolean usable = parent != null
                && parent.getUserType() == UserType.PARENT
                && parent.getStatus() != UserStatus.SUSPENDED
                && parent.getStatus() != UserStatus.DELETED;

        if (!usable) {
            throw new BusinessException(ErrorCode.INVITE_CODE_NOT_FOUND);
        }

        return parent;
    }

    /**
     * (보호자, 아이)에 UNIQUE 제약이 있어 같은 보호자에게 다시 요청하면 INSERT가 실패한다.
     * 거절·취소된 이력이 남아 있으면 그 행을 PENDING으로 되살려 쓴다.
     *
     * @return 요청 ID. 되살린 경우 기존 ID가 그대로 유지된다.
     */
    private Long createOrRevive(Long parentId, Long childId) {
        FamilyRelation existing = familyRelationMapper.selectByParentIdAndChildId(parentId, childId);

        if (existing == null) {
            FamilyRelation relation = FamilyRelation.builder()
                    .parentId(parentId)
                    .childId(childId)
                    .status(FamilyRelationStatus.PENDING)
                    .build();

            familyRelationMapper.insertRequest(relation);

            return relation.getId();
        }

        familyRelationMapper.updateStatus(
                existing.getId(),
                existing.getStatus(),
                FamilyRelationStatus.PENDING
        );

        return existing.getId();
    }
}
