package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.response.MfaResponse;
import kz.qazaqpay.model.entity.MfaCode;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.MfaCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MfaService {

    private final MfaCodeRepository mfaCodeRepository;
    private static final SecureRandom random = new SecureRandom();
    private static final int MFA_EXPIRATION_MINUTES = 5;

    @Transactional
    public MfaResponse generateMfaCode(User user) {
        mfaCodeRepository.deleteByUserAndUsedFalse(user);

        String code = String.format("%06d", random.nextInt(1000000));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(MFA_EXPIRATION_MINUTES);

        MfaCode mfaCode = MfaCode.builder()
                .user(user)
                .code(code)
                .used(false)
                .expiresAt(expiresAt)
                .build();

        mfaCodeRepository.save(mfaCode);

        return MfaResponse.builder()
                .code(code)
                .email(user.getEmail())
                .message("MFA code generated. Valid for " + MFA_EXPIRATION_MINUTES + " minutes.")
                .expiresInSeconds((long) (MFA_EXPIRATION_MINUTES * 60))
                .build();
    }

    @Transactional
    public void verifyMfaCode(User user, String code) {
        MfaCode mfaCode = mfaCodeRepository.findByUserAndCodeAndUsedFalseAndExpiresAtAfter(
                        user, code, LocalDateTime.now())
                .orElseThrow(() -> new RuntimeException("Invalid or expired MFA code"));

        mfaCode.setUsed(true);
        mfaCodeRepository.save(mfaCode);
    }
}
