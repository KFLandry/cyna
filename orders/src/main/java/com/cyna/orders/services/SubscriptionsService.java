package com.cyna.orders.services;

import com.cyna.orders.models.Subscription;
import com.cyna.orders.repositories.SubscriptionRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

// TODO : A tester
@Service
@Transactional
@Log4j2
@RequiredArgsConstructor
public class SubscriptionsService {
    private SubscriptionRepo subscriptionRepo;

    public Subscription create(Subscription subscription) {
        return subscriptionRepo.save(subscription);
    }

    public void delete(Subscription subscription) {
        subscriptionRepo.delete(subscription);
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

    public void update(Subscription subscription) {
        Subscription initialSubscription = subscriptionRepo.findById(subscription.getId()).orElseThrow();

        Subscription updatedSubscription =  Subscription.builder()
                .status(Optional.ofNullable(subscription.getStatus()).orElse(initialSubscription.getStatus()))
                .quantity(Optional.ofNullable(subscription.getQuantity()).orElse(initialSubscription.getQuantity()))
                .amount(Optional.of(subscription.getAmount()).orElse(initialSubscription.getAmount()))
                .paymentMethod(Optional.ofNullable(subscription.getPaymentMethod()).orElse(initialSubscription.getPaymentMethod()))
                .build();

        subscriptionRepo.save(updatedSubscription);
    }

}
