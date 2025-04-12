package com.cyna.subscriptions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodDto {
    private String customerId;
    private String type;
    private long number;
    private long month;
    private long year;
    private long cvc;
}
