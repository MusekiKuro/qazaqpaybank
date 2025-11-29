package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.qazaqpay.model.dto.request.PaymentRequest;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Payments", description = "Service payment endpoints (mobile, internet, utilities)")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/mobile")
    @Operation(summary = "Pay mobile bill", description = "Make mobile phone payment")
    public ResponseEntity<TransferResponse> payMobile(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentService.payMobile(request, user));
    }

    @PostMapping("/internet")
    @Operation(summary = "Pay internet bill", description = "Make internet service payment")
    public ResponseEntity<TransferResponse> payInternet(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentService.payInternet(request, user));
    }

    @PostMapping("/utilities")
    @Operation(summary = "Pay utilities", description = "Make utility bill payment")
    public ResponseEntity<TransferResponse> payUtilities(
            @Valid @RequestBody PaymentRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(paymentService.payUtilities(request, user));
    }
}
