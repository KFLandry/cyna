package com.cyna.subscriptions.repositories;

import com.cyna.subscriptions.dto.TopProduct;
import com.cyna.subscriptions.models.Subscription;
import jakarta.ws.rs.QueryParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepo extends JpaRepository<Subscription, Long> {
    Subscription findByOrderNumber(String orderNumber);

    List<Subscription> findByCustomerId(String customerId);

    void deleteBySubscriptionId(String subscriptionId);

    @Query(value = "SELECT S.product_id as product_id, count(S.product_id) AS sales_number FROM subscription S GROUP BY S.product_id ORDER BY S.product_id DESC LIMIT :top", nativeQuery = true)
    List<TopProduct> getTopsByProductId(@QueryParam("top") int top);
}