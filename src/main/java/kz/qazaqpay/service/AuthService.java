package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.request.LoginRequest;
import kz.qazaqpay.model.dto.request.MfaVerifyRequest;
import kz.qazaqpay.model.dto.request.RegisterRequest;
import kz.qazaqpay.model.dto.response.AuthResponse;
import kz.qazaqpay.model.dto.response.MfaResponse;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.UserRepository;
import kz.qazaqpay.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final AccountService accountService;
    private final MfaService mfaService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepository.existsByIin(request.getIin())) {
            throw new RuntimeException("IIN already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new RuntimeException("Phone number already registered");
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .iin(request.getIin())
                .role(User.UserRole.USER)
                .enabled(true)
                .mfaEnabled(true)
                .build();

        user = userRepository.save(user);
        accountService.createDefaultAccount(user);

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .message("Registration successful. Please login to continue.")
                .build();
    }

    @Transactional
    public Object login(LoginRequest request) { // Изменяем тип возвращаемого значения на Object или создаем общий DTO
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getMfaEnabled()) {
            String token = jwtUtil.generateToken(user);
            // ПРАВИЛЬНОЕ ИСПРАВЛЕНИЕ: ВОЗВРАЩАЕМ AuthResponse С ТОКЕНОМ
            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .message("Login successful. MFA is disabled.")
                    .build();
        }

        return mfaService.generateMfaCode(user);
    }

    @Transactional
    public AuthResponse verifyMfa(MfaVerifyRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        mfaService.verifyMfaCode(user, request.getCode());
        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .message("Login successful")
                .build();
    }
}
