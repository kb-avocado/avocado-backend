package com.avocado.domain.transaction.service;

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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
}
