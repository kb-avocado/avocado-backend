package com.avocado.domain.transfer.controller;

import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.SuccessCode;
import com.avocado.domain.transfer.domain.TransferRecipientSearchType;
import com.avocado.domain.transfer.dto.response.TransferRecipientResponseDto;
import com.avocado.domain.transfer.service.TransferRecipientService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "송금 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transfers")
public class TransferRecipientController {

    private final TransferRecipientService transferRecipientService;

    @ApiOperation(
            value = "송금 대상 조회",
            notes = "지갑 계좌번호 또는 사용자 코드로 송금 대상을 조회합니다."
    )
    @GetMapping("/recipients")
    public ResponseEntity<ApiResponse<TransferRecipientResponseDto>> getRecipient(
            @RequestParam TransferRecipientSearchType searchType,
            @RequestParam String keyword
    ) {
        TransferRecipientResponseDto response = transferRecipientService.findRecipient(
                searchType,
                keyword
        );

        return ResponseEntity
                .status(SuccessCode.TRANSFER_RECIPIENT_FOUND.getHttpStatus())
                .body(ApiResponse.success(SuccessCode.TRANSFER_RECIPIENT_FOUND, response));
    }
}
