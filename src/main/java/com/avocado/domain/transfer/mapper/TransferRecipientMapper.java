package com.avocado.domain.transfer.mapper;

import com.avocado.domain.transfer.domain.TransferRecipientVo;
import com.avocado.domain.transfer.dto.response.RecipientResponseDto;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TransferRecipientMapper {

    RecipientResponseDto findByUserCode(
            @Param("keyword") String keyword
    );

    RecipientResponseDto findByAccountNumber(
            @Param("keyword") String keyword
    );

    /**
     * 아이가 최근 송금한 수취처의 전체 개수를 조회한다.
     *
     * @param childId 아이 회원 ID
     * @return 중복 제거된 수취처 전체 개수
     */
    long countRecentByChildId(
            @Param("childId") Long childId
    );

    /**
     * 아이가 최근 송금한 수취처를 중복 없이 페이지 단위로 조회한다.
     *
     * @param childId 아이 회원 ID
     * @param offset 조회 시작 위치
     * @param size 조회할 수취처 개수
     * @return 최근 송금 수취처 목록
     */
    List<TransferRecipientVo> findRecentByChildId(
            @Param("childId") Long childId,
            @Param("offset") int offset,
            @Param("size") int size
    );
}
