package com.avocado.domain.notification.dto.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
public class NotificationListRequestDto {
    @Min(value = 0, message = "페이지 번호는 최소 0 이상")
    private int page = 0;

    @Min(value = 1, message = "페이지 크기는 최소 1 이상")
    @Max(value = 100, message = "페이지 크기는 최대 100 이하")
    private int size = 20;
}
