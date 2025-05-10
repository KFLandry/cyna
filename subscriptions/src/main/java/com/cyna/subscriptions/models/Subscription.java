package com.cyna.subscriptions.models;

import com.stripe.param.SubscriptionListParams;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // And Id provide by stripe
    @Column(nullable = false, unique = true, name = "subscriptionId")
    private String subscriptionId;

    @JoinColumn(name = "customer_id", nullable = false)
    private String customerId;

    @JoinColumn(name = "productId", nullable = false)
    private Long productId;

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
