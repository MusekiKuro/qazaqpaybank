package kz.qazaqpay.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRPaymentRequest {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "QR data is required")
    private String qrData;
}
