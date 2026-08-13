package com.avocado.domain.home.service;

import com.avocado.domain.home.dto.response.HomeResponseDto;
import com.avocado.global.security.jwt.dto.AuthUser;

public interface HomeService {
    HomeResponseDto getHome(Long childId, AuthUser authUser);
}