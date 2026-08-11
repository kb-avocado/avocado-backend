package com.avocado.domain.piggybank.service;

import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.domain.piggybank.domain.PiggyBankCheerMessage;
import com.avocado.domain.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.domain.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import com.avocado.domain.piggybank.mapper.PiggyBankCheerMessageMapper;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankCheerMessageServiceImpl implements PiggyBankCheerMessageService {

    private final PiggyBankCheerMessageMapper piggyBankCheerMessageMapper;

    @Override
    public PiggyBankCheerMessageResponseDto sendMessage(
            Long piggyBankId,
            PiggyBankCheerMessageCreateRequestDto request,
            AuthUser authUser
    ) {
        // TODO: /api/piggybanks/** permitAll 제거 후에도 방어 로직으로 유지한다.
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        Long parentId = authUser.getUserId();

        PiggyBankCheerMessage cheerMessage = PiggyBankCheerMessage.builder()
                .piggyBankId(piggyBankId)
                .parentId(parentId)
                .content(request.getMessage())
                .build();

        piggyBankCheerMessageMapper.insert(cheerMessage);

        List<PiggyBankCheerMessageResponseDto> messages = piggyBankCheerMessageMapper.selectByPiggyBankId(piggyBankId);

        return messages.get(0);
    }

    @Override
    public List<PiggyBankCheerMessageResponseDto> getMessages(Long piggyBankId) {
        return piggyBankCheerMessageMapper.selectByPiggyBankId(piggyBankId);
    }

    @Override
    public void deleteMessage(Long piggyBankId, Long messageId) {
        int deletedRows = piggyBankCheerMessageMapper.deleteById(messageId);

        if (deletedRows == 0) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }
}