package com.avocado.domain.transaction.service;

import com.avocado.domain.transaction.domain.LedgerType;
import com.avocado.domain.transaction.domain.TransactionStatus;
import com.avocado.domain.transaction.domain.WalletHistoryVo;
import com.avocado.domain.transaction.domain.WalletTransactionType;
import com.avocado.domain.transaction.dto.response.WalletTxDetailResponseDto;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.PageResponse;
import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxItemResponseDto;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.wallet.mapper.WalletMapper;
import com.avocado.global.response.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletTxServiceImpl implements WalletTxService {

    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;

    @Override
    public PageResponse<WalletTxItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto requestDto
    ) {
        // 회원과 연결된 지갑을 조회
        Long walletId = walletMapper.findWalletIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        // 요청한 페이지 정보
        int page = requestDto.getPage();
        int size = requestDto.getSize();

        // 오프셋
        int offset = page * size;

        // 해당 지갑의 총 거래 수
        long totalElements = walletTxMapper.countByWalletId(walletId);

        // 현재 페이지에 해당하는 전체 거래 목록
        List<WalletTxItemResponseDto> items = walletTxMapper.findAllByWalletId(
                walletId,
                offset,
                size
        );

        // 공통 페이지 응답 객체를 생성 후 반환
        return PageResponse.of(
                page,
                size,
                totalElements,
                items
        );
    }

    @Override
    public WalletTxDetailResponseDto getWalletTxDetail(
            Long userId,
            Long transactionId
    ) {
        // 회원과 연결된 지갑을 조회
        Long walletId = walletMapper.findWalletIdByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.WALLET_NOT_FOUND));

        return walletTxMapper.findDetailByWalletIdAndTransactionId(
                walletId,
                transactionId
        ).orElseThrow(() -> new BusinessException(ErrorCode.WALLET_TX_NOT_FOUND));
    }

    /**
     * 내부 선불지갑 송금의 출금 거래와 원장을 기록한다.
     *
     * @param walletId             송금자 지갑 ID
     * @param traceId              연관 거래 추적 ID
     * @param amount               송금 금액
     * @param counterpartyWalletId 수취 지갑 ID
     * @param counterpartyName     수취인 이름
     * @param balanceBefore        거래 전 잔액
     * @param balanceAfter         거래 후 잔액
     */
    @Override
    @Transactional
    public void recordTransferOutToWallet(
            Long walletId,
            String traceId,
            Long amount,
            Long counterpartyWalletId,
            String counterpartyName,
            Long balanceBefore,
            Long balanceAfter
    ) {
        WalletHistoryVo history = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(WalletTransactionType.TRANSFER_OUT)
                .amount(amount)
                .counterpartyWalletId(counterpartyWalletId)
                .counterpartyName(counterpartyName)
                .memo("선불지갑 송금")
                .status(TransactionStatus.SUCCESS)
                .build();

        // 송금 출금 거래 이력을 생성한다.
        Long historyId = createWalletHistory(history);

        // 지갑 잔액 감소를 OUT 원장에 기록한다.
        createWalletLedger(
                historyId,
                walletId,
                LedgerType.OUT,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 내부 선불지갑 송금의 입금 거래와 원장을 기록한다.
     *
     * @param walletId 수취 지갑 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 송금 금액
     * @param counterpartyWalletId 송금자 지갑 ID
     * @param counterpartyName 송금자 이름
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     */
    @Override
    @Transactional
    public void recordTransferInFromWallet(
            Long walletId,
            String traceId,
            Long amount,
            Long counterpartyWalletId,
            String counterpartyName,
            Long balanceBefore,
            Long balanceAfter
    ) {
        WalletHistoryVo history = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(WalletTransactionType.TRANSFER_IN)
                .amount(amount)
                .counterpartyWalletId(counterpartyWalletId)
                .counterpartyName(counterpartyName)
                .memo("선불지갑 송금 수신")
                .status(TransactionStatus.SUCCESS)
                .build();

        // 송금 입금 거래 이력을 생성한다.
        Long historyId = createWalletHistory(history);

        // 수취 지갑 잔액 증가를 IN 원장에 기록한다.
        createWalletLedger(
                historyId,
                walletId,
                LedgerType.IN,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 외부 계좌 송금의 출금 거래와 원장을 기록한다.
     *
     * @param walletId 송금자 지갑 ID
     * @param traceId 연관 거래 추적 ID
     * @param amount 송금 금액
     * @param bankCode 수취 금융기관 코드
     * @param accountNumber 수취 계좌번호
     * @param recipientName 수취인 이름
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     */
    @Override
    @Transactional
    public void recordTransferOutToAccount(
            Long walletId,
            String traceId,
            Long amount,
            String bankCode,
            String accountNumber,
            String recipientName,
            Long balanceBefore,
            Long balanceAfter
    ) {
        WalletHistoryVo history = WalletHistoryVo.builder()
                .walletId(walletId)
                .traceId(traceId)
                .transactionType(WalletTransactionType.TRANSFER_OUT)
                .amount(amount)
                .counterpartyName(recipientName)
                .targetBankCode(bankCode)
                .targetAccountNumber(accountNumber)
                .memo("외부 계좌 송금")
                .status(TransactionStatus.SUCCESS)
                .build();

        // 외부 계좌 송금 출금 이력을 생성
        Long historyId = createWalletHistory(history);

        // 지갑 잔액 감소를 OUT 원장에 기록
        createWalletLedger(
                historyId,
                walletId,
                LedgerType.OUT,
                amount,
                balanceBefore,
                balanceAfter
        );
    }

    /**
     * 선불지갑 거래 이력을 생성한다.
     *
     * @param history 생성할 선불지갑 거래 이력
     * @return 생성된 거래 이력 ID
     * @throws BusinessException 거래 이력 생성에 실패한 경우
     */
    private Long createWalletHistory(
            WalletHistoryVo history
    ) {
        int insertedRows = walletTxMapper.insertWalletHistory(history);

        if (insertedRows != 1 || history.getId() == null) {
            throw new BusinessException(ErrorCode.WALLET_HISTORY_CREATE_FAILED);
        }

        return history.getId();
    }

    /**
     * 선불지갑 거래 원장을 생성한다.
     *
     * @param historyId 거래 이력 ID
     * @param walletId 선불지갑 ID
     * @param ledgerType 잔액 증감 방향
     * @param amount 거래 금액
     * @param balanceBefore 거래 전 잔액
     * @param balanceAfter 거래 후 잔액
     * @throws BusinessException 원장 생성에 실패한 경우
     */
    private void createWalletLedger(
            Long historyId,
            Long walletId,
            LedgerType ledgerType,
            Long amount,
            Long balanceBefore,
            Long balanceAfter
    ) {
        int insertedRows = walletTxMapper.insertWalletLedger(
                historyId,
                walletId,
                ledgerType.name(),
                amount,
                balanceBefore,
                balanceAfter
        );

        if (insertedRows != 1) {
            throw new BusinessException(ErrorCode.WALLET_LEDGER_CREATE_FAILED);
        }
    }
}
