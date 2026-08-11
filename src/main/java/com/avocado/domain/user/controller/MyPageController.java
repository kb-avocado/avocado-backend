package com.avocado.domain.user.controller;

import com.avocado.domain.user.dto.response.MyPageResponseDto;
import com.avocado.domain.user.service.MyPageService;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.avocado.global.response.code.SuccessCode.MY_PAGE_FOUND;

/**
 * 마이페이지 API.
 *
 * 회원가입·로그인은 인증 없이 열려 있어야 해서 /api/auth를 쓰는 UserController에 두지만,
 * 마이페이지는 인증이 필요하므로 경로와 컨트롤러를 분리한다.
 */
@Api(tags = "마이페이지 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class MyPageController {

    private final MyPageService myPageService;

    @ApiOperation(
            value = "내 정보 보기",
            notes = "로그인한 회원의 마이페이지 정보를 조회합니다. "
                    + "(PARENT = 초대 코드, CHILD = 연결된 보호자 정보) "
                    + "값이 없는 필드는 응답에서 제외됩니다."
    )
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponseDto>> getMyPage(
            @AuthenticationPrincipal AuthUser authUser
    ) {
        MyPageResponseDto responseDto = myPageService.getMyPage(authUser);

        return ResponseEntity
                .status(MY_PAGE_FOUND.getHttpStatus())
                .body(ApiResponse.success(MY_PAGE_FOUND, responseDto));
    }
}
