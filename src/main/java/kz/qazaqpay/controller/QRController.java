package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.qazaqpay.model.dto.request.QRPaymentRequest;
import kz.qazaqpay.model.dto.response.QRCodeResponse;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.service.QRService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "QR Payments", description = "QR code generation and payment endpoints")
public class QRController {

    private final QRService qrService;

    @GetMapping("/generate")
    @Operation(summary = "Generate QR code", description = "Generate QR code for receiving payment")
    public ResponseEntity<QRCodeResponse> generateQR(
            @RequestParam String accountNumber,
            @RequestParam BigDecimal amount,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(qrService.generateQRCode(accountNumber, amount, user));
    }

    @PostMapping("/pay")
    @Operation(summary = "Pay by QR code", description = "Make payment by scanning QR code")
    public ResponseEntity<TransferResponse> payByQR(
            @Valid @RequestBody QRPaymentRequest request,
            @AuthenticationPrincipal User user
    ) {
        return ResponseEntity.ok(qrService.payByQR(request, user));
    }
}
