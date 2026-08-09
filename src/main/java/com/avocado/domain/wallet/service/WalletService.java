package com.avocado.domain.wallet.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.domain.UserType;
import com.avocado.domain.wallet.dto.response.WalletResponseDto;
import com.avocado.domain.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletMapper walletMapper;

    /*
     * childId 기준으로 자녀 선불지갑 단건 정보를 조회한다.
     * 인증 사용자 확인, 자녀 존재 확인, 조회 권한 검증, 지갑 존재 확인을 순서대로 처리한다.
     */
    public WalletResponseDto getChildWallet(
            Long childId,
            AuthUser authUser
    ) {
        // TODO: /api/wallets/** permitAll 제거 후에도 방어 로직으로 유지한다.
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!walletMapper.existsChildById(childId)) {
            throw new BusinessException(ErrorCode.CHILD_NOT_FOUND);
        }

        validateChildWalletAccess(childId, authUser);

        return walletMapper.findByChildId(childId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));
    }

    /*
     * 로그인 사용자가 조회 대상 자녀 본인이거나,
     * 해당 자녀와 ACTIVE 가족 관계로 연결된 보호자인지 검증한다.
     */
    private void validateChildWalletAccess(
            Long childId,
            AuthUser authUser
    ) {
        if (isChildOwner(childId, authUser)) {
            return;
        }

        if (isConnectedParent(childId, authUser)) {
            return;
        }

        throw new BusinessException(ErrorCode.FORBIDDEN);
    }

    /*
     * 로그인 사용자가 CHILD 유형이고,
     * 본인의 childId로 지갑을 조회하는 경우인지 확인한다.
     */
    private boolean isChildOwner(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.CHILD.equals(authUser.getUserType())
                && childId.equals(authUser.getUserId());
    }

    /*
     * 로그인 사용자가 PARENT 유형이고,
     * 조회 대상 자녀와 ACTIVE 가족 관계가 있는지 확인한다.
     */
    private boolean isConnectedParent(
            Long childId,
            AuthUser authUser
    ) {
        return UserType.PARENT.equals(authUser.getUserType())
                && walletMapper.existsActiveFamilyRelation(
                        authUser.getUserId(),
                        childId
                );
    }
}
