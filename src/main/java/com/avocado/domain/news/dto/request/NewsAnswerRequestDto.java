package com.avocado.domain.news.dto.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NewsAnswerRequestDto {

    @NotBlank(message="답변을 입력해주세요.")
    @Size(max = 500, message="답변은 500자를 초과할 수 없습니다.")
    private String childAnswer;
}
