package com.avocado.domain.notification.event;


import com.avocado.domain.notification.dto.response.NotificationResponseDto;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class NotificationCreatedEvent {
    private final Long receiverId;

    private final NotificationResponseDto notification;
}
