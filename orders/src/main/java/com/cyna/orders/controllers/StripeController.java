package com.cyna.orders.controllers;

import com.cyna.orders.dto.CustomerDto;
import com.cyna.orders.dto.PriceDto;
import com.cyna.orders.dto.SubscriptionDto;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

// TODO : A Refactoriser et a terminer l'implementation
@RestController
@RequestMapping("/api/v1/subscriptions")
public class StripeController {
    static {

    }
    @Value("${stripe.STRIPE_PUBLISHABLE_KEY}")
    private String stripePublishableKey;

    @Value("${stripe.STRIPE_WEBHOOK_SECRET}")
    private String stripeWebhookSecret;

    @Value("${stripe.STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> config() {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put(
                "publishableKey",
                stripePublishableKey
        );
        return ResponseEntity.ok(responseData);
    }

    @PostMapping("/create-customer")
    public ResponseEntity<String> createCustomer(@ModelAttribute CustomerDto customerDto) {
        Stripe.setAppInfo(
                "stripe-samples/subscription-use-cases/usage-based-subscriptions",
                "0.0.1",
                "https://github.com/stripe-samples/subscription-use-cases/usage-based-subscriptions"
        );
        Stripe.apiKey = this.stripeSecretKey;

        CustomerCreateParams customerParams = CustomerCreateParams
                .builder()
                .setEmail(customerDto.getEmail())
                .setName(customerDto.getName())
                .build();

        try {
            // Create a new customer object
            Customer customer = Customer.create(customerParams);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customer", customer);

            //we use StripeObject.PRETTY_PRINT_GSON.toJson() so that we get the JSON our client is expecting on the polymorphic
            //parameters that can either be object ids or the object themselves. If we tried to generate the JSON without call this,
            //for example, by calling gson.toJson(responseData) we will see something like "customer":{"id":"cus_XXX"} instead of
            //"customer":"cus_XXX".
            //If you only need to return 1 object, you can use the built in serializers, i.e. Subscription.retrieve("sub_XXX").toJson()
            return ResponseEntity.ok(StripeObject.PRETTY_PRINT_GSON.toJson(responseData));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    @PostMapping("/create-price")
    public ResponseEntity<String> createPrice(@ModelAttribute PriceDto priceDto){

        PriceCreateParams priceCreateParams = PriceCreateParams.builder()
                .setCurrency(priceDto.getCurrency())
                .setUnitAmount(priceDto.getAmount())
                .setRecurring(PriceCreateParams.Recurring.builder()
                        .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                        .build())
                .setProductData(PriceCreateParams.ProductData.builder()
                        .setId(priceDto.getProductId())
                        .setName(priceDto.getProductName())
                        .build())
                .build();

        try {
            Price price = Price.create(priceCreateParams);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("price", price);
            return ResponseEntity.ok(StripeObject.PRETTY_PRINT_GSON.toJson(responseData));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    @PostMapping("/create-subscription")
    public ResponseEntity<String> createSubscription(@ModelAttribute SubscriptionDto subscriptionDto){
        // Create the subscription
        SubscriptionCreateParams subCreateParams = SubscriptionCreateParams
                .builder()
                .addItem(
                        SubscriptionCreateParams
                                .Item.builder()
                                .setPrice(subscriptionDto.getPriceId())
                                .build()
                )
                .setCustomer(subscriptionDto.getCustomerId())
                .addAllExpand(Collections.singletonList("pending_setup_intent"))
                .build();

        try {
            Subscription subscription = Subscription.create(subCreateParams);
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("subscription", subscription);
            return ResponseEntity.ok(StripeObject.PRETTY_PRINT_GSON.toJson(responseData));
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> webhook(@RequestHeader("Stripe-Signature") String sigHeader, @RequestBody String payload){
        Event event = null;

        try {
            event = Webhook.constructEvent(payload, sigHeader, this.stripeWebhookSecret);
        } catch (SignatureVerificationException | JsonSyntaxException e) {
            // Invalid signature or payload format
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }
        // Deserialize the nested object inside the event
        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = null;
        if (dataObjectDeserializer.getObject().isPresent()) {
            stripeObject = dataObjectDeserializer.getObject().get();
        } else {
            // Deserialization failed, probably due to an API version mismatch.
            // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
            // instructions on how to handle this case, or return an error here.
        }

        switch (event.getType()) {
            case "invoice.created":
                break;
            case "invoice.paid":
                // Used to provision services after the trial has ended.
                // The status of the invoice will show up as paid. Store the status in your
                // database to reference when a user accesses your service to avoid hitting rate
                // limits.
                break;
            case "invoice.payment_failed":
                // If the payment fails or the customer does not have a valid payment method,
                // an invoice.payment_failed event is sent, the subscription becomes past_due.
                // Use this webhook to notify your user that their payment has
                // failed and to retrieve new card details.
                break;
            case "invoice.finalized":
                // If you want to manually send out invoices to your customers
                // or store them locally to reference to avoid hitting Stripe rate limits.
                break;
            case "customer.subscription.deleted":
                // handle subscription cancelled automatically based
                // upon your subscription settings. Or if the user
                // cancels it.
                break;
            case "customer.subscription.created":
                break;
            case "customer.subscription.updated":
                break;
            case "customer.subscription.resumed":
                break;
            case "customer.subscription.paused":
                break;
            case "customer.subscription.trial_will_end":
                break;
            default:
                // Unhandled event type
        }

        return ResponseEntity.ok("");
    }
}
