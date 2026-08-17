package com.avocado.domain.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 이메일 중복 확인 요청.
 */
@ApiModel(
        value = "EmailCheckRequest",
        description = "이메일 중복 확인 요청 데이터"
)
@Getter
@Setter
@NoArgsConstructor
public class EmailCheckRequestDto {

    @NotBlank(message = "이메일을 필수로 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아니에요.")
    @Size(
            max = 100,
            message = "이메일은 100자 이하로 입력해야 합니다."
    )
    @ApiModelProperty(
            value = "중복을 확인할 이메일",
            example = "hanroro@avocado.kr",
            required = true
    )
    private String email;
}
