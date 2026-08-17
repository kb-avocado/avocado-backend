package com.avocado.domain.piggybank.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class PiggyBankCreateRequestDto {

    @NotBlank(message = "저금통 이름은 필수입니다.")
    private String name;

    @NotNull(message = "목표 금액은 필수입니다.")
    @Positive(message = "목표 금액은 0보다 커야 합니다.")
    private Long targetAmount;

    @Size(max = 20, message = "아이콘 값이 너무 깁니다.")
    private String icon;
}