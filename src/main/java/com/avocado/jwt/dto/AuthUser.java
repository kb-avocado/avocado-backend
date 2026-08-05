package com.avocado.jwt.dto;

import com.avocado.user.domain.UserRole;
import com.avocado.user.domain.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

/**
 * Access Token의 클레임에서 복원한 인증 주체.
 * SecurityContext에 저장되며 컨트롤러에서 @AuthenticationPrincipal로 꺼내 쓴다.
 */
@Getter
@Builder
@AllArgsConstructor
public class AuthUser {

    private final Long userId;
    private final UserRole role;
    private final UserType userType;

    // hasRole('ADMIN') 형태로 검사하려면 ROLE_ 접두사가 필요하다.
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
