package com.avocado.domain.transaction.service;

import com.avocado.global.response.PageResponse;
import com.avocado.domain.transaction.dto.request.WalletTxListRequestDto;
import com.avocado.domain.transaction.dto.response.WalletTxListItemResponseDto;
import com.avocado.domain.transaction.mapper.WalletTxMapper;
import com.avocado.domain.wallet.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WalletTxServiceImpl implements WalletTxService {

    private final WalletMapper walletMapper;
    private final WalletTxMapper walletTxMapper;

    @Override
    public PageResponse<WalletTxListItemResponseDto> getWalletTxList(
            Long userId,
            WalletTxListRequestDto requestDto
    ) {
        // 페이지 처리
        int page = requestDto.getPage();
        int size = requestDto.getSize();
        int offset = page * size;

        // 사용자의 ID로 등록된 지갑의 ID를 조회
        Long walletId = walletMapper
                .findWalletIdByUserId(userId)
                .orElseThrow(
                        () -> new IllegalArgumentException("선불지갑을 찾을 수 없습니다.")
                );

        // 해당 지갑의 전체 거래 수 조회
        long totalElements = walletTxMapper.countByWalletId(walletId);

        // 거래 내역이 존재하지 않을 경우
        if (totalElements == 0) {
            return PageResponse.of(
                    page,
                    size,
                    0L,
                    Collections.emptyList()
            );
        }

        // 해당 지갑의 전체 거래 내역 조회
        List<WalletTxListItemResponseDto> items = walletTxMapper.findAllByWalletId(
                walletId,
                offset,
                size
        );

        // 거래 내역이 존재할 경우
        return PageResponse.of(
                page,
                size,
                totalElements,
                items
        );
    }
}
