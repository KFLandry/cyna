package com.cyna.subscriptions.controllers;

import com.cyna.subscriptions.dto.*;
import com.cyna.subscriptions.models.Subscription;
import com.cyna.subscriptions.services.StripeService;
import com.cyna.subscriptions.services.SubscriptionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
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


    // appelle StripeService pour interroger Stripe, et non plis le subscriptionsService (DB locale)
    @GetMapping(params = "customerId")
    public ResponseEntity<List<SubscriptionDto>> getSubscriptionByCustomerId(@RequestParam("customerId") String customerId){
        // Nouvelle implémentation : appeler le StripeService pour récupérer depuis Stripe
        List<SubscriptionDto> subscriptionsFromStripe = stripeService.listSubscriptionsByCustomer(customerId);

        if (subscriptionsFromStripe.isEmpty()) {
            System.out.println("Aucun abonnement trouvé pour le client ID : " + customerId + " via Stripe.");
            return ResponseEntity.ok(Collections.emptyList());
        }
        System.out.println("Abonnements trouvés pour le client ID " + customerId + " via Stripe : " + subscriptionsFromStripe.size());
        return ResponseEntity.ok(subscriptionsFromStripe);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@RequestBody CustomerDto customerDto) {
        return ResponseEntity.ok(stripeService.createCustomer(customerDto));
    }

    /*Stripe*/
    @PostMapping("/payment-method")
    public ResponseEntity<String> addPaymentMethod(@RequestBody PaymentMethodDto dto) {
         stripeService.attachPaymentMethod(dto);
        return ResponseEntity.ok("PaymentMethod attached");
        }

    @GetMapping("/payment-methods")
    public ResponseEntity<List<PaymentMethodResponseDto>> listPaymentMethods(
            @RequestParam("customerId") String customerId) {
        List<PaymentMethodResponseDto> methods = stripeService.listPaymentMethods(customerId);
        return ResponseEntity.ok(methods);
    }

    @DeleteMapping("/payment-methods/{id}")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable(value = "id") String id) {
            stripeService.detachPaymentMethod(id);
            //204 No content si tout est ok
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/payment-methods/{id}/default")
    public ResponseEntity<Void> setDefaultPaymentMethod(
            @PathVariable("id") String pmId,
            @RequestParam("customerId") String customerId) {
        stripeService.setDefaultPaymentMethod(pmId, customerId);
        return ResponseEntity.noContent().build();
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
        return ResponseEntity.ok(stripeService.cancelSubscription(subscriptionDto.getSubscriptionId()));
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