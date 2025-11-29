package kz.qazaqpay.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String expiryDate; // Format: MM/YY

    @Column(nullable = false)
    private String cvv;

    @Enumerated(EnumType.STRING)
    private CardStatus status;

    private BigDecimal transactionLimit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id")
    private Account account;

    public enum CardStatus {
        ACTIVE,
        BLOCKED
    }
}