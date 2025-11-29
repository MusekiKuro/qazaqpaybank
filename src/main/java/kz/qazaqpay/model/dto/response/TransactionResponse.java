package kz.qazaqpay.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String description;
    private String status;
    private Boolean suspicious;
    private LocalDateTime createdAt;
}
