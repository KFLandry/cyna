package com.cyna.orders.repositories;

import com.cyna.orders.models.Subscription;
import org.hibernate.annotations.SQLDelete;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SubscriptionRepo extends CrudRepository<Subscription, Long> {
    Subscription findByOrderNumber(String orderNumber);

    @SQLDelete(sql = "DELETE FROM Subscription S WHERE S.subscriptionId=:subscriptionId")
    void deleteBySubscriptionId(@Param("subscriptionId") String subscriptionId);
}
