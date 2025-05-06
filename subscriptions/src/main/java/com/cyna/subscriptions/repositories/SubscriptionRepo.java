package com.cyna.subscriptions.repositories;

import com.cyna.subscriptions.models.Subscription;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepo extends CrudRepository<Subscription, Long> {
    Subscription findByOrderNumber(String orderNumber);

    List<Subscription> findByCustomerId(String customerId);

    void deleteBySubscriptionId(String subscriptionId);
}
