package kz.qazaqpay.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class FraudDetectionService {

    private static final BigDecimal SUSPICIOUS_THRESHOLD = new BigDecimal("500000");

    public boolean isSuspicious(BigDecimal amount) {
        return amount.compareTo(SUSPICIOUS_THRESHOLD) > 0;
    }
}
