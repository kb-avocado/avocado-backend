package com.avocado.user.dto.request;

import com.avocado.user.domain.UserType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.validation.constraints.*;
import java.time.LocalDate;

@ApiModel(
        value = "UserSignUpRequest",
        description = "회원가입 요청 데이터"
)
@Getter
@NoArgsConstructor
public class UserSignUpRequestDto {

    @NotBlank(message = "이름은 필수로 입력해주세요.")
    @Size(
            max = 50,
            message = "이름은 최대 50자 이하로 입력해주세요."
    )
    @ApiModelProperty(
            value = "사용자 이름",
            example = "한로로",
            required = true
    )
    private String name;

    @NotBlank(message = "이메일을 필수로 입력해주세요.")
    @Email(message = "올바른 이메일 형식이 아니에요.")
    @Size(
            max = 100,
            message = "이메일은 100자 이하로 입력해야 합니다."
    )
    @ApiModelProperty(
            value = "이메일",
            example = "hanroro@avocado.kr",
            required = true
    )
    private String email;

    @NotBlank(message = "비밀번호를 필수로 입력해주세요.")
    @Size(
            min = 8,
            max = 100,
            message = "비밀번호는 8자 이상 100자 이하로 입력해야 합니다."
    )
    @ApiModelProperty(
            value = "비밀번호",
            example = "hanroro1234",
            required = true
    )
    private String password;

    @NotBlank(message = "전화번호를 필수로 입력해주세요.")
    @Pattern(
            regexp = "^01[016789]-?\\d{3,4}-?\\d{4}$",
            message = "전화번호 형식이 올바르지 않습니다. (예: 01012345678 또는 010-1234-5678)"
    )
    @ApiModelProperty(
            value = "휴대전화 번호",
            example = "01012345678",
            required = true
    )
    private String phone;

    @NotNull(message = "생년월일을 필수로 입력해주세요.")
    @Past(message = "생년월일은 과거 날짜만 가능해요.")
    @ApiModelProperty(
            value = "생년월일",
            example = "2000-01-01",
            notes = "yyyy-MM-dd 형식으로 입력해주세요.",
            required = true
    )
    private LocalDate birth;

    @NotNull
    @ApiModelProperty(
            value = "회원 유형",
            example = "PARENT",
            allowableValues = "PARENT, CHILD",
            required = true
    )
    private UserType type;

}
