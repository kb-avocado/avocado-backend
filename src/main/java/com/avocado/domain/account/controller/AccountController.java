package com.avocado.domain.account.controller;

import com.avocado.domain.account.domain.AccountVo;
import com.avocado.domain.account.dto.request.AccountCreateRequest;
import com.avocado.domain.account.dto.response.AccountCreateResponse;
import com.avocado.domain.account.service.AccountService;
import com.avocado.global.exception.BusinessException;
import com.avocado.global.response.ApiResponse;
import com.avocado.global.response.code.ErrorCode;
import com.avocado.global.security.jwt.dto.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
            @AuthenticationPrincipal AuthUser authUser,
            @Valid @RequestBody AccountCreateRequest request
    ) {
        if (authUser == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        AccountVo account = accountService.createAccount(
                authUser.getUserId(),
                request.getBankCode(),
                request.getAccountNumber()
        );

        AccountCreateResponse response = AccountCreateResponse.from(account);

        return ResponseEntity
                .status(ACCOUNT_CREATED.getHttpStatus())
                .body(ApiResponse.success(ACCOUNT_CREATED, response));
    }
}