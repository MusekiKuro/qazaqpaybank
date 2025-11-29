package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.request.PaymentRequest;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.Transaction;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.AccountRepository;
import kz.qazaqpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    @Transactional
    public TransferResponse payMobile(PaymentRequest request, User user) {
        return processPayment(request, user, Transaction.TransactionType.PAYMENT_MOBILE);
    }

    @Transactional
    public TransferResponse payInternet(PaymentRequest request, User user) {
        return processPayment(request, user, Transaction.TransactionType.PAYMENT_INTERNET);
    }

    @Transactional
    public TransferResponse payUtilities(PaymentRequest request, User user) {
        return processPayment(request, user, Transaction.TransactionType.PAYMENT_UTILITIES);
    }

    private TransferResponse processPayment(PaymentRequest request, User user,
                                            Transaction.TransactionType type) {
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        boolean suspicious = fraudDetectionService.isSuspicious(request.getAmount());

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        String description = String.format("Payment to %s - Account: %s",
                request.getServiceProvider(), request.getServiceAccount());

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(account)
                .amount(request.getAmount())
                .type(type)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(description)
                .suspicious(suspicious)
                .build();

        transaction = transactionRepository.save(transaction);

        return TransferResponse.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccount(account.getAccountNumber())
                .toAccount(request.getServiceProvider())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .suspicious(suspicious)
                .timestamp(transaction.getCreatedAt())
                .message("Payment completed successfully")
                .build();
    }
}
