package com.cyna.orders.dto;

import jakarta.annotation.Nullable;
import lombok.Data;

@Data
public class SubscriptionDto {
    private String priceId;
    private String customerId;
    @Nullable
    private String quantity;
}
