package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kz.qazaqpay.model.dto.response.AccountResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Account", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/balance/{accountNumber}")
    @Operation(summary = "Get account balance", description = "Retrieve balance for a specific account")
    public ResponseEntity<AccountResponse> getBalance(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(accountService.getBalance(accountNumber, user));
    }

    @GetMapping("/my-accounts")
    @Operation(summary = "Get user accounts", description = "Retrieve all active accounts for the authenticated user")
    public ResponseEntity<List<AccountResponse>> getMyAccounts(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(accountService.getUserAccounts(user));
    }
}
