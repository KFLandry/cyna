package com.cyna.subscriptions.models;

import com.stripe.param.SubscriptionListParams;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Table(name = "subscription")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Id stripe
    @Column(nullable = false, unique = true, name = "subscriptionId")
    private String subscriptionId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "productId", nullable = false)
    private Long productId;


    @Column(name = "price_id", nullable = false)
    private String priceId;

    @Column(nullable = false)
    private String orderNumber;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SubscriptionListParams.Status status;

    @Column(nullable = false, name = "quantity")
    private Long quantity;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String paymentMethod;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
}