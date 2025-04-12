package com.cyna.subscriptions.services;

import com.cyna.subscriptions.models.Subscription;
import com.cyna.subscriptions.repositories.SubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class SubscriptionsService {
    private SubscriptionRepo subscriptionRepo;

    public Subscription create(Subscription subscription) {
        return subscriptionRepo.save(subscription);
    }

    public void delete(String subscriptionId) {
        subscriptionRepo.deleteBySubscriptionId(subscriptionId);
    }

    public List<Subscription> findAll() {
        return (List<Subscription>) subscriptionRepo.findAll();
    }

    public Subscription findByOrderNumber(String orderNumber) {
        return subscriptionRepo.findByOrderNumber(orderNumber);
    }

    public Subscription findById(Long id) {
        return subscriptionRepo.findById(id).orElse(null);
    }

    public Subscription update(Subscription subscription) {

        // TODO : Implementer une logique de validation coté client avec de modifier la subscription.

        Subscription initialSubscription = subscriptionRepo.findById(subscription.getId()).orElseThrow();

        Subscription updatedSubscription =  Subscription.builder()
                .status(Optional.ofNullable(subscription.getStatus()).orElse(initialSubscription.getStatus()))
                .quantity(Optional.ofNullable(subscription.getQuantity()).orElse(initialSubscription.getQuantity()))
                .amount(Optional.of(subscription.getAmount()).orElse(initialSubscription.getAmount()))
                .paymentMethod(Optional.ofNullable(subscription.getPaymentMethod()).orElse(initialSubscription.getPaymentMethod()))
                .build();

        return subscriptionRepo.save(updatedSubscription);
    }

}
