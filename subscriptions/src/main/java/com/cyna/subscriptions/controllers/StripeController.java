package com.cyna.subscriptions.controllers;

import com.cyna.subscriptions.dto.CustomerDto;
import com.cyna.subscriptions.dto.PaymentMethodDto;
import com.cyna.subscriptions.dto.PriceDto;
import com.cyna.subscriptions.dto.SubscriptionDto;
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

    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscription(){
        return ResponseEntity.ok(subscriptionsService.findAll());
    }

    @GetMapping(params = "email")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionByCustomer(@RequestParam("customerId") String customer){
        return ResponseEntity.ok(null);
    }

    @GetMapping(params = "subcriptionNumber")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionByCustomerId(@RequestParam("customerId") String subcriptionNumber){
        return ResponseEntity.ok(null);
    }

    @PatchMapping
    public ResponseEntity<SubscriptionDto> updateSubscription(@RequestBody SubscriptionDto subscriptionDto){
        return ResponseEntity.ok(null);
    }
}
