package kz.qazaqpay.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kz.qazaqpay.model.dto.request.LoginRequest;
import kz.qazaqpay.model.dto.request.MfaVerifyRequest;
import kz.qazaqpay.model.dto.request.RegisterRequest;
import kz.qazaqpay.model.dto.response.AuthResponse;
import kz.qazaqpay.model.dto.response.MfaResponse;
import kz.qazaqpay.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "User authentication and registration endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register new user", description = "Create a new user account with automatic account creation")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and generate MFA code")
    public ResponseEntity<MfaResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-mfa")
    @Operation(summary = "Verify MFA code", description = "Verify MFA code and receive JWT token")
    public ResponseEntity<AuthResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        return ResponseEntity.ok(authService.verifyMfa(request));
    }
}
