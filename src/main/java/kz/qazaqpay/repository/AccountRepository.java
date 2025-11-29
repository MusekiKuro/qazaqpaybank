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
    
    // Старые методы (можно оставить, но лучше не использовать)
    List<Account> findByUser(User user);
    
    // НОВЫЙ ПРАВИЛЬНЫЙ МЕТОД: Spring Data сам поймет, что нужно искать по user.id
    List<Account> findByUserIdAndActiveTrue(Long userId);
    
    Boolean existsByAccountNumber(String accountNumber);
}