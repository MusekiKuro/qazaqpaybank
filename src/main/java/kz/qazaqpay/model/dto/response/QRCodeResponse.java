package kz.qazaqpay.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {
    private String qrCodeBase64;
    private String accountNumber;
    private BigDecimal amount;
    private String message;
}
