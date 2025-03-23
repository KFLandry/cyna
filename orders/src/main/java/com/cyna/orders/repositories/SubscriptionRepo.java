package com.cyna.orders.repositories;

import com.cyna.orders.models.Subscription;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepo extends CrudRepository<Subscription, Long> {
    Subscription findByOrderNumber(String orderNumber);
}
