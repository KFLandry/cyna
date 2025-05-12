package com.cyna.subscriptions.repositories;

import com.cyna.subscriptions.dto.TopProduct;
import com.cyna.subscriptions.models.Subscription;
import jakarta.ws.rs.QueryParam;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepo extends CrudRepository<Subscription, Long> {
    Subscription findByOrderNumber(String orderNumber);

    List<Subscription> findByCustomerId(String customerId);

    void deleteBySubscriptionId(String subscriptionId);

    @Query(value = "SELECT count(S.productId) AS salesNumber FROM Subscription S GROUP BY S.productId ORDER BY salesNumber DESC LIMIT :top", nativeQuery = true)
    List<TopProduct> getTopsByProductId(@QueryParam("top") int top);
}
