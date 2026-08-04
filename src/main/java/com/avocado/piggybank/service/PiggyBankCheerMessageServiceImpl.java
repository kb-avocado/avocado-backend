package com.avocado.piggybank.service;

import com.avocado.common.exception.BusinessException;
import com.avocado.common.exception.ErrorCode;
import com.avocado.piggybank.domain.PiggyBankCheerMessage;
import com.avocado.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import com.avocado.piggybank.mapper.PiggyBankCheerMessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PiggyBankCheerMessageServiceImpl implements PiggyBankCheerMessageService {

    private final PiggyBankCheerMessageMapper piggyBankCheerMessageMapper;

    @Override
    public PiggyBankCheerMessageResponseDto sendMessage(Long piggyBankId, PiggyBankCheerMessageCreateRequestDto request) {
        // TODO: parentId는 로그인 붙으면 토큰에서 가져와야 함. 인증 전까지 임시 고정값
        Long parentId = 1L;

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