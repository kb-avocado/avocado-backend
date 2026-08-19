package com.avocado.domain.family.controller;

import com.avocado.global.response.ApiResponse;
import com.avocado.domain.family.domain.FamilyRelationStatus;
import com.avocado.domain.family.dto.request.FamilyRequestConfirmRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestCreateRequestDto;
import com.avocado.domain.family.dto.request.FamilyRequestDecisionRequestDto;
import com.avocado.domain.family.dto.response.FamilyRequestCheckResponseDto;
import com.avocado.domain.family.dto.response.FamilyRequestResponseDto;
import com.avocado.domain.family.service.FamilyService;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

import static com.avocado.global.response.code.SuccessCode.FAMILY_REQUEST_CONFIRMED;
import static com.avocado.global.response.code.SuccessCode.FAMILY_REQUEST_CREATED;
import static com.avocado.global.response.code.SuccessCode.FAMILY_REQUEST_DECIDED;
import static com.avocado.global.response.code.SuccessCode.FAMILY_REQUEST_FOUND;
import static com.avocado.global.response.code.SuccessCode.FAMILY_REQUEST_LIST_FOUND;

@Api(tags = "가족 연결 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/family/requests")
public class FamilyRequestController {

    private final FamilyService familyService;

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
                familyService.createRequest(authUser, request);

        return ResponseEntity
                .status(FAMILY_REQUEST_CREATED.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_CREATED, responseDto));
    }

    @ApiOperation(
            value = "[아이] 가족 연결 요청 조회",
            notes = "아이가 자기 요청의 진행 상태를 확인합니다. 대기 화면에서 주기적으로 호출합니다. "
                    + "보호자가 승인하면 status가 APPROVED로 바뀝니다. "
    )
    @GetMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FamilyRequestResponseDto>> getRequest(
            @AuthenticationPrincipal
            AuthUser authUser,
            @PathVariable
            Long requestId
    ) {
        FamilyRequestResponseDto responseDto =
                familyService.findForChild(authUser, requestId);

        return ResponseEntity
                .status(FAMILY_REQUEST_FOUND.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_FOUND, responseDto));
    }

    @ApiOperation(
            value = "[보호자] 가족 연결 요청 목록",
            notes = "보호자가 자기에게 온 요청을 목록으로 확인합니다. "
                    + "status를 주면 그 상태만, 주지 않으면 상태를 가리지 않고 모두 내려갑니다. "
                    + "최근에 움직인 요청이 앞에 옵니다."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<FamilyRequestCheckResponseDto>>> getRequests(
            @AuthenticationPrincipal
            AuthUser authUser,
            @RequestParam(required = false)
            FamilyRelationStatus status
    ) {
        List<FamilyRequestCheckResponseDto> responseDto =
                familyService.findAllForParent(authUser, status);

        return ResponseEntity
                .status(FAMILY_REQUEST_LIST_FOUND.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_LIST_FOUND, responseDto));
    }

    @ApiOperation(
            value = "[보호자] 가족 연결 요청 확인",
            notes = "보호자가 자기에게 온 요청을 확인합니다."
    )
    @GetMapping("/{requestId}/check")
    public ResponseEntity<ApiResponse<FamilyRequestCheckResponseDto>> checkRequest(
            @AuthenticationPrincipal
            AuthUser authUser,
            @PathVariable
            Long requestId
    ) {
        FamilyRequestCheckResponseDto responseDto =
                familyService.findForParent(authUser, requestId);

        return ResponseEntity
                .status(FAMILY_REQUEST_FOUND.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_FOUND, responseDto));
    }

    @ApiOperation(
            value = "[보호자] 가족 연결 요청 승인·거절",
            notes = "보호자가 자기에게 온 요청을 처리합니다. "
                    + "승인해도 바로 연결되지 않고, 아이가 보호자를 확인해야 확정됩니다. "
                    + "이미 처리한 요청을 다시 처리하면 409입니다."
    )
    @PatchMapping("/{requestId}")
    public ResponseEntity<ApiResponse<FamilyRequestCheckResponseDto>> decideRequest(
            @AuthenticationPrincipal
            AuthUser authUser,
            @PathVariable
            Long requestId,
            @Valid
            @RequestBody
            FamilyRequestDecisionRequestDto request
    ) {
        FamilyRequestCheckResponseDto responseDto =
                familyService.decide(authUser, requestId, request);

        return ResponseEntity
                .status(FAMILY_REQUEST_DECIDED.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_DECIDED, responseDto));
    }

    @ApiOperation(
            value = "[아이] 가족 연결 최종 확정",
            notes = "보호자가 승인한 뒤, 아이가 보호자를 확인하고 연결을 확정하거나 취소합니다."
    )
    @PatchMapping("/{requestId}/confirm")
    public ResponseEntity<ApiResponse<FamilyRequestResponseDto>> confirmRequest(
            @AuthenticationPrincipal
            AuthUser authUser,
            @PathVariable
            Long requestId,
            @Valid
            @RequestBody
            FamilyRequestConfirmRequestDto request
    ) {
        FamilyRequestResponseDto responseDto =
                familyService.confirm(authUser, requestId, request);

        return ResponseEntity
                .status(FAMILY_REQUEST_CONFIRMED.getHttpStatus())
                .body(ApiResponse.success(FAMILY_REQUEST_CONFIRMED, responseDto));
    }
}
