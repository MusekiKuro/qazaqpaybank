package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.request.TransferRequest;
import kz.qazaqpay.model.dto.response.TransactionResponse;
import kz.qazaqpay.model.dto.response.TransferResponse;
import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.Transaction;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.AccountRepository;
import kz.qazaqpay.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final FraudDetectionService fraudDetectionService;

    @Transactional
    public TransferResponse transfer(TransferRequest request, User user) {
        Account fromAccount = accountRepository.findByAccountNumber(request.getFromAccountNumber())
                .orElseThrow(() -> new RuntimeException("Source account not found"));

        Account toAccount = accountRepository.findByAccountNumber(request.getToAccountNumber())
                .orElseThrow(() -> new RuntimeException("Destination account not found"));

        if (!fromAccount.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to source account");
        }

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        boolean suspicious = fraudDetectionService.isSuspicious(request.getAmount());

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .fromAccount(fromAccount)
                .toAccount(toAccount)
                .amount(request.getAmount())
                .type(Transaction.TransactionType.TRANSFER)
                .status(Transaction.TransactionStatus.COMPLETED)
                .description(request.getDescription())
                .suspicious(suspicious)
                .build();

        transaction = transactionRepository.save(transaction);

        return TransferResponse.builder()
                .transactionId(transaction.getTransactionId())
                .fromAccount(fromAccount.getAccountNumber())
                .toAccount(toAccount.getAccountNumber())
                .amount(transaction.getAmount())
                .status(transaction.getStatus().name())
                .suspicious(suspicious)
                .timestamp(transaction.getCreatedAt())
                .message(suspicious ?
                        "Transfer completed but flagged as suspicious" :
                        "Transfer completed successfully")
                .build();
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber, User user, int page, int size) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (!account.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to account");
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionRepository.findByAccount(account, pageable);

        return transactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getTransactionId())
                .type(transaction.getType().name())
                .amount(transaction.getAmount())
                .description(transaction.getDescription())
                .status(transaction.getStatus().name())
                .suspicious(transaction.getSuspicious())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}
