package kz.qazaqpay.repository;

import kz.qazaqpay.model.entity.MfaCode;
import kz.qazaqpay.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface MfaCodeRepository extends JpaRepository<MfaCode, Long> {
    Optional<MfaCode> findByUserAndCodeAndUsedFalseAndExpiresAtAfter(
            User user, String code, LocalDateTime currentTime);
    void deleteByUserAndUsedFalse(User user);
}
