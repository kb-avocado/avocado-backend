package com.avocado.domain.home.controller;

import com.avocado.domain.home.dto.response.HomeResponseDto;
import com.avocado.domain.home.service.HomeService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.avocado.global.response.code.SuccessCode.HOME_FOUND;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    @GetMapping
    public ResponseEntity<ApiResponse<HomeResponseDto>> getHome(
            @RequestParam(required = false) Long childId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        HomeResponseDto data = homeService.getHome(childId, authUser);

        return ResponseEntity
                .status(HOME_FOUND.getHttpStatus())
                .body(ApiResponse.success(HOME_FOUND, data));
    }
}