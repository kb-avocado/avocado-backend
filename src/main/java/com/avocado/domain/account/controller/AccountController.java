package com.avocado.domain.account.controller;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.dto.request.AccountCreateRequest;
import com.avocado.domain.account.dto.response.AccountCreateResponse;
import com.avocado.domain.account.service.AccountService;
import com.avocado.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import static com.avocado.global.response.code.SuccessCode.ACCOUNT_CREATED;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    /**
     * 부모 회원 은행 계좌 연동 API
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AccountCreateResponse>> createAccount(
            @Valid @RequestBody AccountCreateRequest request
    ) {
        // 임시 아이디
        Long mockId = 1L;

        AccountVo account = accountService.createAccount(
                mockId,
                request.getBankCode(),
                request.getAccountNumber()
        );

        AccountCreateResponse response = AccountCreateResponse.from(account);

        return ResponseEntity
                .status(ACCOUNT_CREATED.getHttpStatus())
                .body(ApiResponse.success(ACCOUNT_CREATED, response));
    }
}