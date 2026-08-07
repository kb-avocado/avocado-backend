package com.avocado.account.dto.request;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountCreateRequest {

    @NotBlank(message = "은행 코드는 필수입니다.")
    @Pattern(
            regexp = "^[0-9]{3}$",
            message = "은행 코드는 3자리 숫자여야 합니다."
    )
    private String bankCode;

    @NotBlank(message = "계좌번호는 필수입니다.")
    @Size(
            max = 20,
            message = "계좌번호는 20자 이하여야 합니다."
    )
    @Pattern(
            regexp = "^[0-9]+$",
            message = "계좌번호는 숫자만 입력할 수 있습니다."
    )
    private String accountNumber;
}
