package kz.qazaqpay.model.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class CardResponse {
    private Long id;
    private String cardNumber;
    private String expiryDate;
    private String cardHolder;
    private String status;
    private BigDecimal limit;
    private String accountNumber;
}