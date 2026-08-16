package com.avocado.domain.transfer.service;

import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.domain.TransferRecipientVo;
import com.avocado.domain.transfer.dto.request.TransferRecipientListRequestDto;
import com.avocado.domain.transfer.dto.response.RecipientResponseDto;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.mapper.TransferRecipientMapper;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import com.avocado.global.response.code.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferRecipientServiceImplTest {

    @Mock
    private TransferRecipientMapper transferRecipientMapper;


    @InjectMocks
    private TransferRecipientServiceImpl transferRecipientService;


    @Test
    @DisplayName("사용자 코드로 송금 대상을 조회한다")
    void findRecipientByUserCode() {
        // given
        String keyword = "AVO1234";
        RecipientResponseDto recipient = recipient("12345678901234");

        when(transferRecipientMapper.findByUserCode(keyword)).thenReturn(recipient);

        // when
        RecipientResponseDto result = transferRecipientService.findRecipient(
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
        RecipientResponseDto recipient = recipient(keyword);

        when(transferRecipientMapper.findByAccountNumber(keyword)).thenReturn(recipient);

        // when
        RecipientResponseDto result = transferRecipientService.findRecipient(
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

    /**
     * 최근 송금 수취처를 페이지 정보와 함께 반환하는지 확인한다.
     */
    @Test
    @DisplayName("최근 송금 수취처 페이지 조회")
    void getRecentRecipients() {
        // given
        Long childId = 102L;

        TransferRecipientListRequestDto requestDto =
                new TransferRecipientListRequestDto();

        requestDto.setPage(1);
        requestDto.setSize(1);

        // Mapper가 반환할 최근 수취처 VO를 생성한다.
        TransferRecipientVo recipient =
                TransferRecipientVo.builder()
                        .recipientName("김지호")
                        .bankCode("999")
                        .recipientNumber("9990000002")
                        .transferredAt(
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        6,
                                        14,
                                        0
                                )
                        )
                        .build();

        // 최근 수취처 전체 개수는 2건이라고 가정한다.
        when(
                transferRecipientMapper.countRecentByChildId(
                        childId
                )
        ).thenReturn(2L);

        // page=1, size=1이므로 offset=1로 두 번째 수취처를 조회한다.
        when(
                transferRecipientMapper.findRecentByChildId(
                        childId,
                        1,
                        1
                )
        ).thenReturn(
                List.of(recipient)
        );

        // when
        PageResponse<TransferRecipientResponseDto> response =
                transferRecipientService.getRecentRecipients(
                        childId,
                        requestDto
                );

        // then
        assertThat(response.getPage())
                .isEqualTo(1);

        assertThat(response.getSize())
                .isEqualTo(1);

        assertThat(response.getTotalElements())
                .isEqualTo(2L);

        // 총 2건을 한 페이지에 1건씩 조회하므로 전체 페이지는 2개이다.
        assertThat(response.getTotalPages())
                .isEqualTo(2L);

        assertThat(response.getItems())
                .hasSize(1);

        TransferRecipientResponseDto item =
                response.getItems().get(0);

        assertThat(item.getRecipientName())
                .isEqualTo("김지호");

        assertThat(item.getBankCode())
                .isEqualTo("999");

        assertThat(item.getBankName())
                .isEqualTo("아보카도");

        assertThat(item.getRecipientNumber())
                .isEqualTo("9990000002");

        // Service가 page * size로 offset을 계산했는지 확인한다.
        verify(transferRecipientMapper)
                .findRecentByChildId(
                        childId,
                        1,
                        1
                );
    }

    private RecipientResponseDto recipient(String accountNumber) {
        RecipientResponseDto recipient = new RecipientResponseDto();
        recipient.setRecipientType("WALLET");
        recipient.setRecipientId(2001L);
        recipient.setRecipientName("김효정");
        recipient.setAccountNumber(accountNumber);
        recipient.setUserCode("AVO1234");
        return recipient;
    }
}
