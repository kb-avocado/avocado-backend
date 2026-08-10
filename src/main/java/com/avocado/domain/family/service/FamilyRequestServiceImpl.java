package com.avocado.domain.family.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.family.domain.FamilyRelation;
import com.avocado.domain.family.domain.FamilyRelationStatus;
import com.avocado.domain.family.dto.request.FamilyRequestConfirmRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestDecision;
import com.avocado.domain.family.dto.request.FamilyRequestDecisionRequestDto;
import com.avocado.domain.family.dto.response.FamilyRequestCheckResponseDto;
import com.avocado.domain.family.dto.response.FamilyRequestResponseDto;
import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.family.mapper.FamilyWalletMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.User;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyRequestServiceImpl implements FamilyRequestService {

    private final FamilyRelationMapper familyRelationMapper;
    private final FamilyWalletMapper familyWalletMapper;
    private final UserMapper userMapper;

    /**
     * 아이가 보호자의 초대 코드로 가족 연결을 요청한다.
     *
     * @param authUser 요청한 아이 (토큰에서 꺼낸 인증 주체)
     * @param request   초대 코드
     * @return 만들어진 요청 정보
     * @throws BusinessException 아이 계정이 아니거나, 초대 코드가 없거나, 이미 연결된 경우
     */
    @Override
    @Transactional
    public FamilyRequestResponseDto createRequest(
            AuthUser authUser,
            FamilyRequestCreateRequestDto request
    ) {
        requireAuthenticated(authUser);

        if (authUser.getUserType() != UserType.CHILD) {
            throw new BusinessException(ErrorCode.CHILD_ONLY_FAMILY_REQUEST);
        }

        Long childId = authUser.getUserId();

        // 아이 하나에 보호자 하나. 이미 연결됐으면 초대 코드를 찾아볼 것도 없다.
        if (familyRelationMapper.existsActiveByChildId(childId)) {
            throw new BusinessException(ErrorCode.ALREADY_CONNECTED);
        }

        User parent = findParentByInviteCode(request.getCode());

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
    @Override
    public FamilyRequestResponseDto findForChild(AuthUser authUser, Long requestId) {
        requireAuthenticated(authUser);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(authUser, relation.getChildId());

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
    @Override
    public FamilyRequestCheckResponseDto findForParent(AuthUser authUser, Long requestId) {
        requireAuthenticated(authUser);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(authUser, relation.getParentId());

        return FamilyRequestCheckResponseDto.builder()
                .requestId(relation.getId())
                .status(relation.getStatus())
                .childName(relation.getChildName())
                .createdAt(relation.getCreatedAt())
                .build();
    }

    /**
     * 보호자가 자기에게 온 요청을 승인하거나 거절한다.
     *
     * @throws BusinessException 요청이 없거나(404), 본인에게 온 요청이 아니거나(403),
     *                           이미 처리된 요청인 경우(409)
     */
    @Override
    @Transactional
    public FamilyRequestCheckResponseDto decide(
            AuthUser authUser,
            Long requestId,
            FamilyRequestDecisionRequestDto request
    ) {
        requireAuthenticated(authUser);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(authUser, relation.getParentId());

        if (relation.getStatus() != FamilyRelationStatus.PENDING) {
            throw new BusinessException(ErrorCode.FAMILY_REQUEST_ALREADY_HANDLED);
        }

        FamilyRelationStatus decided = request.getDecision() == FamilyRequestDecision.APPROVE
                ? FamilyRelationStatus.APPROVED
                : FamilyRelationStatus.REJECTED;

        // 조회와 수정 사이에 다른 요청이 먼저 처리했을 수 있다. 바뀐 행이 없으면 그 경우다.
        int updated = familyRelationMapper.updateStatus(
                relation.getId(),
                FamilyRelationStatus.PENDING,
                decided
        );

        if (updated == 0) {
            throw new BusinessException(ErrorCode.FAMILY_REQUEST_ALREADY_HANDLED);
        }

        return FamilyRequestCheckResponseDto.builder()
                .requestId(relation.getId())
                .status(decided)
                .childName(relation.getChildName())
                .createdAt(relation.getCreatedAt())
                .build();
    }

    /**
     * 아이가 보호자를 확인하고 연결을 확정하거나 취소한다. 가족 연결의 마지막 단계다.
     * 확정하면 관계가 ACTIVE가 되고, 아이 계정도 함께 ACTIVE로 바뀐다.
     * 두 테이블을 함께 바꾸므로 하나라도 실패하면 전부 되돌린다.
     *
     * @throws BusinessException 요청이 없거나(404), 본인 요청이 아니거나(403),
     *                           보호자가 아직 승인하지 않은 경우(409)
     */
    @Override
    @Transactional
    public FamilyRequestResponseDto confirm(
            AuthUser authUser,
            Long requestId,
            FamilyRequestConfirmRequestDto request
    ) {
        requireAuthenticated(authUser);

        FamilyRelation relation = findRelation(requestId);
        requireOwner(authUser, relation.getChildId());

        if (relation.getStatus() != FamilyRelationStatus.APPROVED) {
            throw new BusinessException(ErrorCode.FAMILY_REQUEST_NOT_APPROVED);
        }

        FamilyRelationStatus confirmed = request.getConfirm()
                ? FamilyRelationStatus.ACTIVE
                : FamilyRelationStatus.CANCELED;

        // 확인 버튼을 연타해도 한 번만 통과한다.
        int updated = familyRelationMapper.updateStatus(
                relation.getId(),
                FamilyRelationStatus.APPROVED,
                confirmed
        );

        if (updated == 0) {
            throw new BusinessException(ErrorCode.FAMILY_REQUEST_NOT_APPROVED);
        }

        // 연결이 끝나야 아이가 서비스를 쓸 수 있다. 취소한 경우에는 PENDING으로 남는다.
        if (confirmed == FamilyRelationStatus.ACTIVE) {
            userMapper.updateStatus(relation.getChildId(), UserStatus.ACTIVE);
            createWallet(relation.getChildId());
        }

        return FamilyRequestResponseDto.builder()
                .requestId(relation.getId())
                .status(confirmed)
                .parentName(relation.getParentName())
                .build();
    }

    /**
     * 아이의 선불지갑을 만든다.
     * 확정과 같은 트랜잭션이라 여기서 실패하면 관계와 계정 상태까지 함께 되돌아간다.
     */
    private void createWallet(Long childId) {
        if (familyWalletMapper.existsByChildId(childId)) {
            throw new BusinessException(ErrorCode.WALLET_ALREADY_EXISTS);
        }

        familyWalletMapper.insertWallet(childId, temporaryWalletNumber(childId));
    }

    /**
     * TODO: 지갑 번호 규칙은 지갑 담당자가 정한다. 지금은 UNIQUE 제약만 지키는 임시값이다.
     */
    private String temporaryWalletNumber(Long childId) {
        return "WALLET-" + childId;
    }

    private void requireAuthenticated(AuthUser authUser) {
        if (authUser == null) {
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
    private void requireOwner(AuthUser authUser, Long ownerId) {
        if (!authUser.getUserId().equals(ownerId)) {
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
