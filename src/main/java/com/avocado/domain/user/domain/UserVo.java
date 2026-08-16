package com.avocado.domain.user.domain;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo {
    private Long id;

    private String name;

    private UserType userType;

    private UserRole role;

    private UserStatus status;
}
