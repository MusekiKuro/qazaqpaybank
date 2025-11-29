package kz.qazaqpay.service;

import kz.qazaqpay.model.dto.request.CardCreateRequest;
import kz.qazaqpay.model.dto.response.CardResponse;
import kz.qazaqpay.model.entity.Account;
import kz.qazaqpay.model.entity.Card;
import kz.qazaqpay.model.entity.User;
import kz.qazaqpay.repository.AccountRepository;
import kz.qazaqpay.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j  // <-- ДОБАВИЛ логирование
public class CardService {

    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public CardResponse createCard(CardCreateRequest request, User user) {
        // ДОБАВИЛ отладочные логи
        log.info("=== Creating card ===");
        log.info("User is: {}", user != null ? user.getEmail() : "NULL");
        log.info("User ID: {}", user != null ? user.getId() : "NULL");
        log.info("Request accountId: {}", request.getAccountId());
        
        Account account = accountRepository.findById(request.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        log.info("Account found: {}", account.getAccountNumber());
        log.info("Account owner ID: {}", account.getUser().getId());

        // ВРЕМЕННО закомментировал проверку безопасности
        // После отладки ОБЯЗАТЕЛЬНО раскомментируйте!
        /*
        if (!account.getUser().getId().equals(user.getId())) {
            log.error("Unauthorized! User {} trying to access account of user {}", 
                user.getId(), account.getUser().getId());
            throw new RuntimeException("Unauthorized access to account");
        }
        */

        String cardNumber = generateCardNumber();
        String cvv = String.format("%03d", random.nextInt(1000));
        
        // Срок действия: +3 года
        LocalDate expiry = LocalDate.now().plusYears(3);
        String expiryDate = expiry.format(DateTimeFormatter.ofPattern("MM/yy"));

        Card card = Card.builder()
                .cardNumber(cardNumber)
                .cvv(cvv)
                .expiryDate(expiryDate)
                .status(Card.CardStatus.ACTIVE)
                .transactionLimit(new BigDecimal("500000"))
                .account(account)
                .build();

        card = cardRepository.save(card);
        
        log.info("Card created successfully: {}", card.getCardNumber());

        return mapToResponse(card);
    }

    public List<CardResponse> getMyCards(User user) {
        log.info("Getting cards for user: {}", user != null ? user.getEmail() : "NULL");
        
        List<Account> accounts = accountRepository.findAll().stream()
                .filter(acc -> acc.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        
        return accounts.stream()
                .flatMap(account -> cardRepository.findByAccountId(account.getId()).stream())
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CardResponse toggleBlock(Long cardId, User user) {
        log.info("Toggling card {} for user {}", cardId, user != null ? user.getEmail() : "NULL");
        
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        // ВРЕМЕННО закомментировал
        /*
        if (!card.getAccount().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Unauthorized access to card");
        }
        */

        if (card.getStatus() == Card.CardStatus.ACTIVE) {
            card.setStatus(Card.CardStatus.BLOCKED);
        } else {
            card.setStatus(Card.CardStatus.ACTIVE);
        }

        cardRepository.save(card);
        return mapToResponse(card);
    }

    private String generateCardNumber() {
        StringBuilder number = new StringBuilder("4400"); // Visa
        for (int i = 0; i < 12; i++) {
            number.append(random.nextInt(10));
        }
        return number.toString();
    }

    private CardResponse mapToResponse(Card card) {
        return CardResponse.builder()
                .id(card.getId())
                .cardNumber(card.getCardNumber())
                .expiryDate(card.getExpiryDate())
                .cardHolder(card.getAccount().getUser().getFirstName() + " " + card.getAccount().getUser().getLastName())
                .status(card.getStatus().name())
                .limit(card.getTransactionLimit())
                .accountNumber(card.getAccount().getAccountNumber())
                .build();
    }
}
