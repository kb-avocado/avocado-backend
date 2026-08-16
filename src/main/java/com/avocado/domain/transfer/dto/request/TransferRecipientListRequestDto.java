package com.avocado.domain.transfer.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 최근 송금 수취처 목록의 페이지 조회 조건을 전달
 */
@Getter
@Setter
public class TransferRecipientListRequestDto {

    // 조회할 페이지 번호
    @Min(value = 0, message = "페이지 번호는 최소 0 이상")
    private int page = 0;

    // 한 페이지에 조회할 수취처 개수
    @Min(value = 1, message = "페이지 크기는 최소 1 이상")
    @Max(value = 100, message = "페이지 크기는 최대 100 이하")
    private int size = 10;
}
