package kz.qazaqpay.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MfaResponse {
    private String code;
    private String message;
    private String email;
    private Long expiresInSeconds;
}
