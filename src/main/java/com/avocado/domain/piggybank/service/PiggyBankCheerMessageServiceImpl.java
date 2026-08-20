package com.avocado.domain.piggybank.service;

import com.avocado.domain.notification.domain.NotificationType;
import com.avocado.domain.notification.service.NotificationService;
import com.avocado.domain.piggybank.domain.PiggyBank;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.domain.PiggyBankCheerMessage;
import com.avocado.domain.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import com.avocado.domain.piggybank.mapper.PiggyBankCheerMessageMapper;
import com.avocado.domain.piggybank.mapper.PiggyBankMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankCheerMessageServiceImpl implements PiggyBankCheerMessageService {

    private final PiggyBankCheerMessageMapper piggyBankCheerMessageMapper;
    private final PiggyBankMapper piggyBankMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PiggyBankCheerMessageResponseDto sendMessage(
            Long piggyBankId,
            PiggyBankCheerMessageCreateRequestDto request,
            AuthUser authUser,
            Long walletId
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 소유권 검증: 이 저금통이 요청자(또는 요청자가 조회 중인 아이)의 지갑 소속인지
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);
        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        Long parentId = authUser.getUserId();

        PiggyBankCheerMessage cheerMessage = PiggyBankCheerMessage.builder()
                .piggyBankId(piggyBankId)
                .parentId(parentId)
                .content(request.getMessage())
                .build();

        piggyBankCheerMessageMapper.insert(cheerMessage);

        Long childId = piggyBankCheerMessageMapper.selectChildIdByPiggyBankId(piggyBankId);
        if (childId == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }

        notificationService.create(
                childId,
                NotificationType.CHEER_MESSAGE_RECEIVED,
                request.getMessage()
        );

        List<PiggyBankCheerMessageResponseDto> messages = piggyBankCheerMessageMapper.selectByPiggyBankId(piggyBankId);

        return messages.get(0);
    }

    @Override
    public List<PiggyBankCheerMessageResponseDto> getMessages(Long piggyBankId, Long walletId) {
        PiggyBank piggyBank = piggyBankMapper.selectById(piggyBankId);
        if (piggyBank == null) {
            throw new BusinessException(ErrorCode.PIGGY_BANK_NOT_FOUND);
        }
        if (!piggyBank.getWalletId().equals(walletId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return piggyBankCheerMessageMapper.selectByPiggyBankId(piggyBankId);
    }

    @Override
    public void deleteMessage(Long piggyBankId, Long messageId, AuthUser authUser) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        int deletedRows = piggyBankCheerMessageMapper.deleteById(messageId, authUser.getUserId());

        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}