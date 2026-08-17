package com.avocado.domain.transaction.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
public class WalletTxListRequestDto {

    @Min(value = 0)
    private int page = 0;

    @Min(value = 1)
    @Max(value = 30)
    private int size = 20;
}
