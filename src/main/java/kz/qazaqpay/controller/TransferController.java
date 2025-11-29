package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.qazaqpay.model.dto.request.TransferRequest;
import kz.qazaqpay.model.dto.response.TransactionResponse;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.service.TransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Transfers & Transactions", description = "Money transfer and transaction history endpoints")
public class TransferController {

    private final TransferService transferService;

    @PostMapping("/transfer")
    @Operation(summary = "Transfer money", description = "Transfer money between internal accounts")
    public ResponseEntity<TransferResponse> transfer(
            @Valid @RequestBody TransferRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transferService.transfer(request, user));
    }

    @GetMapping("/transactions/{accountNumber}")
    @Operation(summary = "Get transaction history", description = "Retrieve transaction history for an account")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(transferService.getTransactionHistory(accountNumber, user, page, size));
    }
}
