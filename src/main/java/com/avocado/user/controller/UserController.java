package com.avocado.user.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.jwt.component.JwtTokenProvider;
import com.avocado.jwt.component.JwtUtil;
import com.avocado.user.domain.LoginResultCode;
import com.avocado.user.domain.UserRole;
import com.avocado.user.domain.UserStatus;
import com.avocado.user.dto.request.UserLoginRequestDto;
import com.avocado.user.dto.request.UserSignUpRequestDto;
import com.avocado.user.dto.response.LoginUserDto;
import com.avocado.user.dto.response.UserLoginResponseDto;
import com.avocado.user.dto.response.UserSignUpResponseDto;
import com.avocado.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Api(tags = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;

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

    @ApiOperation(
            value = "로그인",
            notes = "이메일과 비밀번호로 로그인합니다. "
                    + "Access Token은 응답 본문이 아니라 HttpOnly 쿠키로 전달됩니다. "
                    + "계정이 PENDING이면 로그인은 성공하되 code가 달라집니다."
    )
    @PostMapping("/auth/login")
    public ApiResponse<UserLoginResponseDto> login(
            @Valid
            @RequestBody
            UserLoginRequestDto request,
            HttpServletResponse response
    ) {
        UserLoginResponseDto responseDto = userService.login(request);
        LoginUserDto user = responseDto.getUser();

        // 토큰 발급과 쿠키 전달은 HTTP 계층의 관심사라 컨트롤러에서 처리한다.
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getRole(),
                user.getType()
        );

        jwtUtil.addAccessTokenCookie(
                response,
                accessToken,
                jwtTokenProvider.getAccessTokenValidity()
        );

        LoginResultCode result = LoginResultCode.of(user.getType(), user.getStatus());

        return ApiResponse.success(
                result.getCode(),
                result.getMessage(),
                responseDto
        );
    }

    @ApiOperation(
            value = "로그아웃",
            notes = "Access Token 쿠키를 만료시킵니다. "
                    + "무상태 JWT라 서버에 남는 정보가 없으므로 쿠키 삭제만으로 로그아웃이 완료됩니다."
    )
    @PostMapping("/auth/logout")
    public ApiResponse<Void> logout(HttpServletResponse response) {
        // 토큰이 이미 만료된 상태에서도 쿠키는 지울 수 있어야 하므로 인증을 요구하지 않는다.
        jwtUtil.expireAccessTokenCookie(response);

        return ApiResponse.success(
                "LOGOUT_SUCCESS",
                "로그아웃되었습니다."
        );
    }
}
