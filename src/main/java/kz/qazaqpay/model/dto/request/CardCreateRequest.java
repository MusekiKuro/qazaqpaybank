package kz.qazaqpay.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CardCreateRequest {
    @NotNull(message = "Account ID is required")
    private Long accountId;
}