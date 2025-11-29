package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.response.AccountResponse;
import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public Account createDefaultAccount(User user) {
        String accountNumber = generateAccountNumber();

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .balance(BigDecimal.ZERO)
                .accountType(Account.AccountType.SAVINGS)
                .currency("KZT")
                .active(true)
                .user(user)
                .build();

        return accountRepository.save(account);
    }

    public AccountResponse getBalance(String accountNumber, User user) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        return mapToResponse(account);
    }

    public List<AccountResponse> getUserAccounts(User user) {
        // ИСПРАВЛЕНИЕ: Передаем ID пользователя, а не весь объект
        return accountRepository.findByUserIdAndActiveTrue(user.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private String generateAccountNumber() {
        String prefix = "KZ";
        StringBuilder number = new StringBuilder(prefix);

        for (int i = 0; i < 18; i++) {
            number.append(random.nextInt(10));
        }

        String accountNumber = number.toString();

        if (accountRepository.existsByAccountNumber(accountNumber)) {
            return generateAccountNumber();
        }

        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .accountType(account.getAccountType().name())
                .currency(account.getCurrency())
                .active(account.getActive())
                .build();
    }
}