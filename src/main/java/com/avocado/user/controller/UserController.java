package com.avocado.user.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.user.domain.UserRole;
import com.avocado.user.domain.UserStatus;
import com.avocado.user.dto.request.UserSignUpRequestDto;
import com.avocado.user.dto.response.UserSignUpResponseDto;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    @ApiOperation(
            value = "회원가입",
            notes = "사용자의 회원가입 요청을 처리합니다."
    )
    @PostMapping("/auth/signup")
    public ApiResponse<UserSignUpResponseDto> signUp(
            @Valid
            @RequestBody
            UserSignUpRequestDto request
    ) {
        UserSignUpResponseDto responseDto = UserSignUpResponseDto.builder()
                .userId(101L)
                .name(request.getName())
                .type(request.getType())
                .role(UserRole.USER)
                .status(UserStatus.ACTIVE)
                .build();

        return ApiResponse.success(
                "SIGNUP_SUCCESS",
                "회원가입 성공",
                responseDto
        );
    }
}
