package com.cyna.subscriptions.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodResponseDto {
    private String id;
    private String last4;
    private int expiryMonth;
    private int expiryYear;
    private String type;
    private String cardholderName;
    @JsonProperty("isDefault") //soucis d'intéraction/sérialisation Lombok-> JSON de br1
    private boolean isDefault;
}
