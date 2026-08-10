package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.dto.request.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.mapper.TransferRecipientMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.code.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferRecipientServiceImplTest {

    @Mock
    private TransferRecipientMapper transferRecipientMapper;

    private TransferRecipientService transferRecipientService;

    @BeforeEach
    void setUp() {
        transferRecipientService = new TransferRecipientServiceImpl(transferRecipientMapper);
    }

    @Test
    @DisplayName("사용자 코드로 송금 대상을 조회한다")
    void findRecipientByUserCode() {
        // given
        String keyword = "AVO1234";
        TransferRecipientResponseDto recipient = recipient("12345678901234");

        when(transferRecipientMapper.findByUserCode(keyword)).thenReturn(recipient);

        // when
        TransferRecipientResponseDto result = transferRecipientService.findRecipient(
                TransferRecipientSearchType.USER_CODE,
                keyword
        );

        // then
        assertThat(result.getRecipientType()).isEqualTo("WALLET");
        assertThat(result.getRecipientId()).isEqualTo(2001L);
        assertThat(result.getRecipientName()).isEqualTo("김효정");
        assertThat(result.getAccountNumber()).isEqualTo("1234-****-1234");
        assertThat(result.getUserCode()).isEqualTo(keyword);
        verify(transferRecipientMapper).findByUserCode(keyword);
        verify(transferRecipientMapper, never()).findByAccountNumber(keyword);
    }

    @Test
    @DisplayName("계좌번호로 송금 대상을 조회한다")
    void findRecipientByAccountNumber() {
        // given
        String keyword = "12345678901234";
        TransferRecipientResponseDto recipient = recipient(keyword);

        when(transferRecipientMapper.findByAccountNumber(keyword)).thenReturn(recipient);

        // when
        TransferRecipientResponseDto result = transferRecipientService.findRecipient(
                TransferRecipientSearchType.ACCOUNT_NUMBER,
                keyword
        );

        // then
        assertThat(result.getAccountNumber()).isEqualTo("1234-****-1234");
        verify(transferRecipientMapper, never()).findByUserCode(keyword);
        verify(transferRecipientMapper).findByAccountNumber(keyword);
    }

    @Test
    @DisplayName("검색어가 비어 있으면 INVALID_REQUEST 예외를 반환한다")
    void findRecipientBlankKeyword() {
        // when & then
        assertThatThrownBy(() -> transferRecipientService.findRecipient(
                TransferRecipientSearchType.USER_CODE,
                " "
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);

        verify(transferRecipientMapper, never()).findByUserCode(" ");
    }

    @Test
    @DisplayName("송금 대상을 찾지 못하면 TRANSFER_RECIPIENT_NOT_FOUND 예외를 반환한다")
    void findRecipientNotFound() {
        // given
        String keyword = "AVO9999";

        when(transferRecipientMapper.findByUserCode(keyword)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> transferRecipientService.findRecipient(
                TransferRecipientSearchType.USER_CODE,
                keyword
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TRANSFER_RECIPIENT_NOT_FOUND);
    }

    @Test
    @DisplayName("검색 유형이 없으면 INVALID_REQUEST 예외를 반환한다")
    void findRecipientInvalidSearchType() {
        // when & then
        assertThatThrownBy(() -> transferRecipientService.findRecipient(
                null,
                "AVO1234"
        ))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.INVALID_REQUEST);
    }

    private TransferRecipientResponseDto recipient(String accountNumber) {
        TransferRecipientResponseDto recipient = new TransferRecipientResponseDto();
        recipient.setRecipientType("WALLET");
        recipient.setRecipientId(2001L);
        recipient.setRecipientName("김효정");
        recipient.setAccountNumber(accountNumber);
        recipient.setUserCode("AVO1234");
        return recipient;
    }
}
