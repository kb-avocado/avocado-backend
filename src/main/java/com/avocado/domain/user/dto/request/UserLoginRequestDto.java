package com.avocado.domain.user.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@ApiModel(
        value = "UserLoginRequest",
        description = "로그인 요청 데이터"
)
@Getter
@NoArgsConstructor
public class UserLoginRequestDto {

    @NotBlank(message = "이메일을 필수로 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아니에요.")
    @ApiModelProperty(
            value = "이메일",
            example = "parent1@example.com",
            required = true
    )
    private String email;

    // 로그인에서는 비밀번호 형식을 검증하지 않는다. 정책이 바뀌어도 기존 회원이 막히면 안 되고,
    // 형식 오류와 비밀번호 불일치를 구분해서 알려줄 이유도 없다.
    @NotBlank(message = "비밀번호를 필수로 입력해주세요.")
    @ApiModelProperty(
            value = "비밀번호",
            example = "avocado1234",
            required = true
    )
    private String password;
}
