package com.cyna.subscriptions.dto;

import lombok.Data;

@Data
public class SubscriptionDto {
    private String priceId;
    private String customerId;
    private long quantity;
}