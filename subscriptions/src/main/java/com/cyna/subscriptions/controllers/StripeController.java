package com.cyna.subscriptions.controllers;

import com.cyna.subscriptions.dto.*;
import com.cyna.subscriptions.models.Subscription;
import com.cyna.subscriptions.services.StripeService;
import com.cyna.subscriptions.services.SubscriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class StripeController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private SubscriptionsService subscriptionsService;

    @GetMapping("/config")
    public ResponseEntity<Map<String, String>> config() {
        return ResponseEntity.ok(stripeService.getConfig());
    }

    @GetMapping("/{customerId}/customer-portal")
    public ResponseEntity<String> customerPortal(@PathVariable String customerId) {
        return ResponseEntity.ok(stripeService.getCustomerPortal(customerId));
    }

    @GetMapping("/{customerId}/ephemeral-key")
    public ResponseEntity<String> ephemeralKey(@PathVariable String customerId) {
        return ResponseEntity.ok(stripeService.getEphemeralKey(customerId));
    }
    
    @GetMapping("/")
    public ResponseEntity<List<Subscription>> getSubscription(){
        return ResponseEntity.ok(subscriptionsService.findAll());
    }

    @GetMapping({"/{id}"})
    public ResponseEntity<Subscription> getSubscriptionById(@PathVariable(value = "id") long id){
        return ResponseEntity.ok(subscriptionsService.findById(id));
    }

    @GetMapping(params = "customerId")
    public ResponseEntity<List<Subscription>> getSubscriptionByCustomerId(@RequestParam("customerId") String customerId){
        return ResponseEntity.ok(subscriptionsService.findByCustomerId(customerId));
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(stripeService.createCustomer(customerDto));
    }

    @PostMapping("/payment-method")
    public ResponseEntity<String> addPaymentMethod(@RequestBody PaymentMethodDto paymentMethodDto) {
        return ResponseEntity.ok(stripeService.addPaymentMethod(paymentMethodDto));
    }

    @PostMapping("/create-price")
    public ResponseEntity<PriceDto> createPrice(@RequestBody PriceDto priceDto){
       return ResponseEntity.ok(stripeService.createPrice(priceDto));
    }

    @PostMapping("/create-subscription")
    public ResponseEntity<String> createSubscription(@RequestBody SubscriptionDto subscriptionDto){
        return ResponseEntity.ok(stripeService.createSubscription(subscriptionDto));
    }

    @PostMapping("/subscription/cancel")
    public ResponseEntity<Object> cancelSubscription(@RequestBody SubscriptionDto subscriptionDto){
        return ResponseEntity.ok(stripeService.cancelSubscription(subscriptionDto.getCustomerId()));
    }

    @PostMapping("/webhook")
    public ResponseEntity<Object> webhook(@RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload){
        return ResponseEntity.ok(stripeService.handleWebhook(sigHeader, payload));
    }

    @PatchMapping("/subscriptionId")
    public ResponseEntity<SubscriptionDto> updateSubscription(@RequestBody SubscriptionDto subscriptionDto){
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteSubscription(@PathVariable long id){
        return  ResponseEntity.ok(subscriptionsService.delete(id));
    }

    @GetMapping(value = "/top-products", params = "top")
    public ResponseEntity<List<TopProduct>> getTopProducts(@RequestParam int top){
        return ResponseEntity.ok(subscriptionsService.getTopProducts(top));
    }
}
