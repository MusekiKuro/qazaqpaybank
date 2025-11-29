package kz.qazaqpay.repository;

import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findByUser(User user);
    List<Account> findByUserAndActiveTrue(User user);
    Boolean existsByAccountNumber(String accountNumber);
}
