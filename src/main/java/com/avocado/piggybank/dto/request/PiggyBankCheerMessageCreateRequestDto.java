package com.avocado.piggybank.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class PiggyBankCheerMessageCreateRequestDto {

    @NotBlank(message = "응원 메시지를 입력해주세요.")
    @Size(max = 200, message = "응원 메시지는 200자를 초과할 수 없습니다.")
    private String message;
}