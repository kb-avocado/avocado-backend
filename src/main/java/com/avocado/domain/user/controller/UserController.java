package com.avocado.domain.user.controller;

import com.avocado.domain.user.repository.RefreshTokenRepository;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.component.JwtTokenProvider;
import com.avocado.global.security.jwt.component.JwtUtil;
import com.avocado.global.security.jwt.dto.AuthUser;
import com.avocado.domain.user.dto.request.UserLoginRequestDto;
import com.avocado.domain.user.dto.request.UserSignUpRequestDto;
import com.avocado.domain.user.dto.response.LoginUserDto;
import com.avocado.domain.user.dto.response.UserSignUpResponseDto;
import com.avocado.domain.user.service.UserLoginService;
import com.avocado.domain.user.service.UserSignUpService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.*;

@Api(tags = "회원 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class UserController {

    private final UserLoginService userLoginService;
    private final UserSignUpService userSignUpService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    @ApiOperation(
            value = "회원가입",
            notes = "사용자의 회원가입 요청을 처리합니다."
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserSignUpResponseDto>> signUp(
            @Valid
            @RequestBody
            UserSignUpRequestDto request,
            HttpServletResponse response
    ) {
        UserSignUpResponseDto responseDto = userSignUpService.signUp(request);

        // Access Token
        String accessToken = jwtTokenProvider.createAccessToken(
                responseDto.getUserId(),
                responseDto.getRole(),
                responseDto.getType()
        );
        jwtUtil.addAccessTokenCookie(response, accessToken);

        // Refresh Token
        String refreshToken = refreshTokenRepository.issue(responseDto.getUserId());
        jwtUtil.addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity
                .status(SIGNUP_SUCCESS.getHttpStatus())
                .body(ApiResponse.success(SIGNUP_SUCCESS, responseDto));
    }

    @ApiOperation(
            value = "로그인",
            notes = "이메일과 비밀번호로 로그인합니다. "
                    + "Access Token은 응답 본문이 아니라 HttpOnly 쿠키로 전달됩니다. "
                    + "이동할 화면은 응답의 status와 type으로 판단합니다. "
                    + "(PENDING + PARENT = 계좌 연결 필요, PENDING + CHILD = 가족 연결 필요)"
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginUserDto>> login(
            @Valid
            @RequestBody
            UserLoginRequestDto request,
            HttpServletResponse response
    ) {
        LoginUserDto user = userLoginService.login(request);

        // Access Token
        String accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getRole(),
                user.getType()
        );
        jwtUtil.addAccessTokenCookie(response, accessToken);

        // Refresh Token
        String refreshToken = refreshTokenRepository.issue(user.getUserId());
        jwtUtil.addRefreshTokenCookie(response, refreshToken);

        return ResponseEntity
                .status(LOGIN_SUCCESS.getHttpStatus())
                .body(ApiResponse.success(LOGIN_SUCCESS, user));
    }

    @ApiOperation(
            value = "내 로그인 정보 조회",
            notes = "쿠키의 Access Token으로 로그인한 회원의 정보를 다시 조회합니다. "
                    + "새로고침하면 브라우저에 있던 회원 정보가 사라지는데, 토큰은 HttpOnly 쿠키라 JS가 읽을 수 없어 서버에 되물어야 합니다. "
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<LoginUserDto>> me(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        LoginUserDto user = userLoginService.me(authUser);

        return ResponseEntity
                .status(SESSION_FOUND.getHttpStatus())
                .body(ApiResponse.success(SESSION_FOUND, user));
    }

    @ApiOperation(
            value = "로그아웃",
            notes = "Access Token 쿠키를 만료시킵니다. "
                    + "무상태 JWT라 서버에 남는 정보가 없으므로 쿠키 삭제만으로 로그아웃이 완료됩니다."
    )
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // 토큰이 이미 만료된 상태에서도 쿠키는 지울 수 있어야 하므로 인증을 요구하지 않는다.
        jwtUtil.expireAccessTokenCookie(response);

        return ResponseEntity
                .status(LOGOUT_SUCCESS.getHttpStatus())
                .body(ApiResponse.success(LOGOUT_SUCCESS));
    }
}
