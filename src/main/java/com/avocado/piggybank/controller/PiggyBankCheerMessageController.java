package com.avocado.piggybank.controller;

import com.avocado.common.response.ApiResponse;
import com.avocado.piggybank.dto.request.PiggyBankCheerMessageCreateRequestDto;
import com.avocado.piggybank.dto.response.PiggyBankCheerMessageResponseDto;
import com.avocado.piggybank.service.PiggyBankCheerMessageService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/piggybanks/{piggyBankId}/cheer-messages")
@RequiredArgsConstructor
@Api(tags = "저금통 응원 메시지 API")
public class PiggyBankCheerMessageController {

    private final PiggyBankCheerMessageService piggyBankCheerMessageService;

    @PostMapping
    @ApiOperation(value = "응원 메시지 전송", notes = "보호자가 저금통에 응원 메시지를 남깁니다.")
    public ApiResponse<PiggyBankCheerMessageResponseDto> sendMessage(
            @PathVariable Long piggyBankId,
            @Valid @RequestBody PiggyBankCheerMessageCreateRequestDto request
    ) {
        PiggyBankCheerMessageResponseDto response = piggyBankCheerMessageService.sendMessage(piggyBankId, request);

        return ApiResponse.success("CHEER_MESSAGE_SENT", "응원 메시지가 전송되었습니다.", response);
    }

    @GetMapping
    @ApiOperation(value = "응원 메시지 조회", notes = "저금통에 남겨진 응원 메시지 목록을 조회합니다.")
    public ApiResponse<List<PiggyBankCheerMessageResponseDto>> getMessages(
            @PathVariable Long piggyBankId
    ) {
        List<PiggyBankCheerMessageResponseDto> messages = piggyBankCheerMessageService.getMessages(piggyBankId);

        return ApiResponse.success("CHEER_MESSAGE_FETCHED", "응원 메시지 조회에 성공했습니다.", messages);
    }

    @DeleteMapping("/{messageId}")
    @ApiOperation(value = "응원 메시지 삭제", notes = "보호자가 본인이 작성한 응원 메시지를 삭제합니다.")
    public ApiResponse<Void> deleteMessage(
            @PathVariable Long piggyBankId,
            @PathVariable Long messageId
    ) {
        piggyBankCheerMessageService.deleteMessage(piggyBankId, messageId);

        return ApiResponse.success("CHEER_MESSAGE_DELETED", "응원 메시지가 삭제되었습니다.");
    }
}