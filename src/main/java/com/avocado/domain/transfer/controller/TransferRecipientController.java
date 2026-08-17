package com.avocado.domain.transfer.controller;

import com.avocado.domain.transfer.dto.request.TransferRecipientListRequestDto;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.PageResponse;
import com.avocado.global.response.code.SuccessCode;
import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.response.RecipientResponseDto;
import com.avocado.domain.transfer.service.TransferRecipientService;
import com.avocado.global.security.jwt.dto.AuthUser;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.*;

@Api(tags = "송금 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers/recipients")
public class TransferRecipientController {

    private final TransferRecipientService transferRecipientService;

//    @ApiOperation(
//            value = "송금 대상 조회",
//            notes = "지갑 계좌번호 또는 사용자 코드로 송금 대상을 조회합니다."
//    )
//    @GetMapping
//    public ResponseEntity<ApiResponse<RecipientResponseDto>> getRecipient(
//            @RequestParam TransferRecipientSearchType searchType,
//            @RequestParam String keyword
//    ) {
//        RecipientResponseDto response = transferRecipientService.findRecipient(
//                searchType,
//                keyword
//        );
//
//        return ResponseEntity
//                .status(TRANSFER_RECIPIENT_FOUND.getHttpStatus())
//                .body(ApiResponse.success(TRANSFER_RECIPIENT_FOUND, response));
//    }

    /**
     * 인증된 아이의 최근 송금 수취처를 페이지 단위로 조회한다.
     *
     * @param authUser   인증 사용자
     * @param requestDto 페이지 조회 조건
     * @return 최근 송금 수취처 페이지 응답
     */
    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<PageResponse<TransferRecipientResponseDto>>> getRecentRecipients(
            @AuthenticationPrincipal AuthUser authUser,
            @Valid TransferRecipientListRequestDto requestDto
    ) {
        Long childId = authUser.getUserId();

        PageResponse<TransferRecipientResponseDto> response = transferRecipientService.getRecentRecipients(
                childId,
                requestDto
        );

        return ResponseEntity
                .status(TRANSFER_RECENT_RECIPIENTS_FOUND.getHttpStatus())
                .body(ApiResponse.success(TRANSFER_RECENT_RECIPIENTS_FOUND, response));
    }
}
