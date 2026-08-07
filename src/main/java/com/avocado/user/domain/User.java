package com.avocado.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

// users 테이블 매핑 도메인
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long id;
    private String email;
    // 단방향 해시된 비밀번호 (BCrypt)
    private String password;
    private String name;
    private String phone;
    private LocalDate birth;
    private UserType userType;
    private UserRole role;
    private UserStatus status;
    private String inviteCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
