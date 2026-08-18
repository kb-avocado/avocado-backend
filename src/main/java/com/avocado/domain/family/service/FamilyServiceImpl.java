package com.avocado.domain.family.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.domain.family.domain.FamilyRelation;
import com.avocado.domain.family.domain.FamilyRelationStatus;
import com.avocado.domain.family.dto.request.FamilyRequestConfirmRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestDecision;
import com.avocado.domain.family.dto.request.FamilyRequestDecisionRequestDto;
import com.avocado.domain.family.dto.response.FamilyRequestCheckResponseDto;
import com.avocado.domain.family.dto.response.FamilyRequestResponseDto;
import com.avocado.domain.family.mapper.FamilyRelationMapper;
import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.User;
import com.avocado.domain.user.domain.UserStatus;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.user.mapper.UserMapper;
import com.avocado.domain.user.service.UserService;
import com.avocado.domain.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import static com.avocado.global.response.code.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FamilyServiceImpl implements FamilyService {

    private final FamilyRelationMapper familyRelationMapper;
    private final WalletService walletService;
    private final UserMapper userMapper;
    private final UserService userService;
    private final NotificationService notificationService;

    /**
     * 아이가 보호자의 초대 코드로 가족 연결을 요청한다.
     *
     * @param authUser 요청한 아이 (토큰에서 꺼낸 인증 주체)
     * @param request  초대 코드
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
            throw new BusinessException(CHILD_ONLY_FAMILY_REQUEST);
        }

        Long childId = authUser.getUserId();

        // 아이 하나에 보호자 하나. 이미 연결됐으면 초대 코드를 찾아볼 것도 없다.
        if (familyRelationMapper.existsActiveByChildId(childId)) {
            throw new BusinessException(ALREADY_CONNECTED);
        }

        User parent = findParentByInviteCode(request.getCode());

        familyRelationMapper.cancelInProgressByChildId(childId);

        Long requestId = createOrRevive(parent.getId(), childId);

        notificationService.create(
                parent.getId(),
                NotificationType.FAMILY_INVITE_RECEIVED,
                String.format("%s님이 가족 연결을 요청했습니다.", userService.getUserName(childId))
        );

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
            throw new BusinessException(FAMILY_REQUEST_ALREADY_HANDLED);
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
            throw new BusinessException(FAMILY_REQUEST_ALREADY_HANDLED);
        }

        if (decided == FamilyRelationStatus.APPROVED) {
            notificationService.create(
                    relation.getChildId(),
                    NotificationType.FAMILY_RELATION_APPROVED,
                    String.format("%s님이 가족 연결을 승인했습니다.", relation.getParentName())
            );
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
            throw new BusinessException(FAMILY_REQUEST_NOT_APPROVED);
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
            throw new BusinessException(FAMILY_REQUEST_NOT_APPROVED);
        }

        // 연결이 끝나야 아이가 서비스를 쓸 수 있다. 취소한 경우에는 PENDING으로 남는다.
        if (confirmed == FamilyRelationStatus.ACTIVE) {
            userService.activate(relation.getChildId());
            walletService.issueWallet(relation.getChildId());
        }

        return FamilyRequestResponseDto.builder()
                .requestId(relation.getId())
                .status(confirmed)
                .parentName(relation.getParentName())
                .build();
    }

    /**
     * 부모와 아이가 ACTIVE 가족 관계인지 검증한다.
     */
    @Override
    public void validateActiveRelation(
            Long parentId,
            Long childId
    ) {
        boolean isFamily = familyRelationMapper.existsActiveRelation(
                parentId,
                childId
        );

        if (!isFamily) {
            throw new BusinessException(FAMILY_RELATION_NOT_FOUND);
        }
    }

    private void requireAuthenticated(AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(UNAUTHORIZED);
        }
    }

    private FamilyRelation findRelation(Long requestId) {
        FamilyRelation relation = familyRelationMapper.selectDetailById(requestId);

        if (relation == null) {
            throw new BusinessException(FAMILY_REQUEST_NOT_FOUND);
        }

        return relation;
    }

    /**
     * 요청 ID 숫자만 바꾸면 남의 가족 요청에 닿을 수 있다.
     * 조회든 처리든 당사자인지 반드시 확인한다.
     */
    private void requireOwner(AuthUser authUser, Long ownerId) {
        if (!authUser.getUserId().equals(ownerId)) {
            throw new BusinessException(FORBIDDEN);
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
            throw new BusinessException(INVITE_CODE_NOT_FOUND);
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

        int updated = familyRelationMapper.updateStatus(
                existing.getId(),
                existing.getStatus(),
                FamilyRelationStatus.PENDING
        );

        // 조회와 갱신 사이에 다른 요청이 같은 행을 건드리면 0건이 된다.
        if (updated == 0) {
            requirePending(existing.getId());
        }

        return existing.getId();
    }

    /**
     * 되살리기가 반영되지 않았을 때 실제 상태를 다시 확인한다.
     * 요청 버튼을 연타한 경우 다른 호출이 이미 PENDING으로 만들어 놨으므로 실패가 아니다.
     * 그 사이 보호자가 응답하는 등 다른 상태가 됐다면, PENDING이라고 응답할 수 없으므로 중단한다.
     */
    private void requirePending(Long requestId) {
        FamilyRelation current = findRelation(requestId);

        if (current.getStatus() != FamilyRelationStatus.PENDING) {
            throw new BusinessException(FAMILY_REQUEST_ALREADY_HANDLED);
        }
    }
}
