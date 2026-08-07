package com.avocado.family.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.family.dto.response.FamilyRequestResponseDto;
import com.avocado.family.service.FamilyRequestService;
import com.avocado.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.avocado.common.response.code.SuccessCode.FAMILY_REQUEST_CREATED;

@Api(tags = "가족 연결 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family/requests")
public class FamilyRequestController {

    private final FamilyRequestService familyRequestService;

    @ApiOperation(
            value = "가족 연결 요청",
            notes = "아이가 보호자의 초대 코드로 연결을 요청합니다. "
                    + "같은 보호자에게 다시 요청하면 이전 요청이 되살아나 요청 ID가 유지됩니다."
    )
    @PostMapping
    public ResponseEntity<ApiResponse<FamilyRequestResponseDto>> createRequest(
            @AuthenticationPrincipal
            AuthUser authUser,
            @Valid
            @RequestBody
            FamilyRequestCreateRequestDto request
    ) {
        FamilyRequestResponseDto responseDto =
                familyRequestService.createRequest(authUser, request);

        return ResponseEntity
                .status(FAMILY_REQUEST_CREATED.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_CREATED, responseDto));
    }
}
