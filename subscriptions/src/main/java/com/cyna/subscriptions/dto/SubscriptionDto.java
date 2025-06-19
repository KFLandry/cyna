package com.cyna.subscriptions.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionDto {
    private String subscriptionId;
    private String customerId;
    private String priceId;
    private Long quantity;
    private Long amount;
    private String productName;
    private String pricingModel;
    private String status;
    private Long createdAt;
}