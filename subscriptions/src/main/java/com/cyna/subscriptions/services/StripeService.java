package com.cyna.subscriptions.services;

import com.cyna.subscriptions.dto.*;
import com.google.gson.JsonSyntaxException;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import com.stripe.param.*;
import com.stripe.param.billingportal.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripeService {

    @Value("${stripe.STRIPE_PUBLISHABLE_KEY}")
    private String stripePublishableKey;

    @Value("${stripe.STRIPE_WEBHOOK_SECRET}")
    private String stripeWebhookSecret;

    @Value("${stripe.STRIPE_SECRET_KEY}")
    private String stripeSecretKey;

    @Value("${stripe.return_url}")
    private String stripeReturnUrl;

    @Value("${stripe.STRIPE_API_VERSION}")
    private String stripeApiVersion;

    private final SubscriptionsService subscriptionService;
    private final MailerSendService mailerSendService;
    private final DiscoveryClient discoveryClient;
    private final RestClient.Builder restClientBuilder;
    private final String AUTH_USERS_ID = "auth-users";

    @PostConstruct
    public void init() {
        Stripe.setAppInfo("stripe-samples/subscription-use-cases/usage-based-subscriptions", "0.0.1",
                "https://github.com/stripe-samples/subscription-use-cases/usage-based-subscriptions");
        Stripe.apiKey = this.stripeSecretKey;
    }

    public String createCustomer(CustomerDto customerDto) {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(customerDto.getEmail())
                .setName(customerDto.getName())
                .build();
        try {
            Customer customer = Customer.create(params);

            // MAJ du champ customerId dans la table User
            UserDto userDto =  UserDto.builder()
                    .id(customerDto.getUserId())
                    .customerId(customer.getId()).build();
            this.updateUser(userDto);

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("customer", userDto);
            return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public PriceDto createPrice(PriceDto priceDto) {
        PriceCreateParams params = switch (priceDto.getPricingModel()) {
            case ONE_TIME -> PriceCreateParams.builder()
                    .setCurrency(priceDto.getCurrency())
                    .setUnitAmount(priceDto.getAmount())
                    .setProductData(PriceCreateParams.ProductData.builder()
                            .setName(priceDto.getProductName())
                            .putMetadata("productId", priceDto.getProductId())
                            .build())
                    .build();
            case PER_MONTH_PER_DEVICE, PER_MONTH_PER_USER -> PriceCreateParams.builder()
                    .setCurrency(priceDto.getCurrency())
                    .setUnitAmount(priceDto.getAmount())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                            .build())
                    .setProductData(PriceCreateParams.ProductData.builder()
                            .setName(priceDto.getProductName())
                            .putMetadata("productId", priceDto.getProductId())
                            .build())
                    .build();
            case PER_YEAR_PER_DEVICE, PER_YEAR_PER_USER -> PriceCreateParams.builder()
                    .setCurrency(priceDto.getCurrency())
                    .setUnitAmount(priceDto.getAmount())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.YEAR)
                            .build())
                    .setProductData(PriceCreateParams.ProductData.builder()
                            .setName(priceDto.getProductName())
                            .putMetadata("productId", priceDto.getProductId())
                            .build())
                    .build();
            default ->
                    throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Unsupported pricing model: " + priceDto.getPricingModel());
        };

        try {
            Price price = Price.create(params);
            priceDto.setPriceId(price.getId());
            return priceDto;
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public String createSubscription(SubscriptionDto subscriptionDto) {
        // On fait bien attention de mettre le comportement de paiement à DEFAULT_INCOMPLETE
        SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                .addItem(SubscriptionCreateParams.Item.builder()
                        .setPrice(subscriptionDto.getPriceId())
                        .setQuantity(subscriptionDto.getQuantity())
                        .build())
                .setCustomer(subscriptionDto.getCustomerId())
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE) // <-- important !
                .addAllExpand(Collections.singletonList("latest_invoice.payment_intent")) // <-- important !
                .build();
        try {
            Subscription subscription = Subscription.create(params);
            Map<String, Object> responseData = new HashMap<>();
            Invoice latestInvoice = Invoice.retrieve(subscription.getLatestInvoice());
            responseData.put("customerId", subscription.getCustomer());
            responseData.put("clientSecret", PaymentIntent.retrieve(latestInvoice.getPaymentIntent()).getClientSecret());
            return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", stripePublishableKey);
        return config;
    }

    public Subscription cancelSubscription(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            return subscription.cancel();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public Object handleWebhook(String sigHeader, String payload) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException | JsonSyntaxException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject == null) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), "Unable to deserialize webhook event object.");
        }

        switch (event.getType()) {
            case "invoice.paid":
                processInvoicePaid((Invoice) stripeObject);
                break;
            case "invoice.payment_failed":
                processInvoicePaymentFailed((Invoice) stripeObject);
                break;
            case "invoice.finalized":
                processInvoiceFinalized((Invoice) stripeObject);
                break;
            case "customer.deleted":
                processCustomerDeleted( (Customer) stripeObject);
                break;
            case "customer.updated":
                processCustomerUpdated((Customer)stripeObject );
                break;
            case "customer.subscription.canceled":
                processSubscriptionCanceled((Subscription) stripeObject);
                break;
            case "payment_method.updated":
                processPaymentMethodUpdated((PaymentMethod) stripeObject);
                break;
            case "customer.subscription.created":
                return processSubscriptionCreated((Subscription) stripeObject);
            case "customer.subscription.updated":
            case "customer.subscription.resumed":
            case "customer.subscription.paused":
            case "customer.subscription.trial_will_end":
                processSubscriptionUpdated((Subscription) stripeObject);
                break;
            case "setup_intent.succeeded":
                processSetupIntentSucceeded((Subscription) stripeObject);
                break;
            case "setup_intend.failed":
                // Traitement de l'échec du setup, à définir si besoin.
                break;
            default:
                // Événement non géré.
        }
        return null;
    }

    private void processCustomerUpdated(Customer customer) {
        Address address =  customer.getAddress();
        AddressDto addressDto =  AddressDto.builder()
                .customer_Id(customer.getId())
                .name(address.getLine1())
                .city(address.getCity())
                .country(address.getCountry())
                .postcode(address.getPostalCode())
                .build();

        String tokenRelayed =  this.getTokenRelay();
        if (tokenRelayed != null && tokenRelayed.startsWith("Bearer ")) {
            String result = restClientBuilder.build()
                    .patch()
                    .uri(this.getServiceURI(AUTH_USERS_ID) + "/api/v1/address")
                    .header(HttpHeaders.AUTHORIZATION, tokenRelayed)
                    .body(addressDto)
                    .retrieve()
                    .body(String.class);

            log.info("[StripeService][processCustomerUpdated] result {}", result);
        }

        mailerSendService.sendEmail(customer.getEmail(), this.stripeReturnUrl, "customer.updated");
    }

    private void processCustomerDeleted(Customer customer) {
        // On ne supprime pas complete l'user, mais on defini son customerId à NULL
        UserDto userDto =  UserDto.builder()
                .customerId(null)
                .build();
        this.updateUser(userDto);
        mailerSendService.sendEmail(customer.getEmail(), this.stripeReturnUrl, "customer.deleted");
    }

    private void processInvoicePaid(Invoice invoice) {
        if (invoice == null) {
            throw new NullPointerException("Invoice is null in invoice.paid event");
        }
        String customerEmail = invoice.getCustomerEmail();
        String portalUrl = getCustomerPortal(invoice.getCustomer());
        mailerSendService.sendEmail(customerEmail, portalUrl, "invoice.paid");
    }

    private void processInvoicePaymentFailed(Invoice invoice) {
        if (invoice == null) {
            throw new NullPointerException("Invoice is null in invoice.payment_failed event");
        }
        String customerEmail = invoice.getCustomerEmail();
        String portalUrl = getCustomerPortal(invoice.getCustomer());
        mailerSendService.sendEmail(customerEmail, portalUrl, "invoice.payment_failed");
    }

    private void processInvoiceFinalized(Invoice invoice) {
        if (invoice == null) {
            throw new NullPointerException("Invoice is null in invoice.finalized event");
        }
        String customerEmail = invoice.getCustomerEmail();
        mailerSendService.sendEmail(customerEmail, invoice.getInvoicePdf(), "invoice.finalized");
    }

    private void processSubscriptionCanceled(Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("Subscription is null in customer.subscription.canceled event");
        }
        subscriptionService.delete(subscription.getId());
        String portalUrl = getCustomerPortal(subscription.getCustomer());
        mailerSendService.sendEmail(subscription.getCustomerObject().getEmail(), portalUrl, "customer.subscription.canceled");
    }

    private void processPaymentMethodUpdated(PaymentMethod paymentMethod) {
        if (paymentMethod == null) {
            throw new NullPointerException("PaymentMethod is null in payment_method.updated event");
        }
        com.cyna.subscriptions.models.Subscription updatedSubscription = com.cyna.subscriptions.models.Subscription.builder()
                .customerId(paymentMethod.getCustomer())
                .paymentMethod(paymentMethod.getType())
                .build();
        subscriptionService.update(updatedSubscription);
        mailerSendService.sendEmail(paymentMethod.getCustomerObject().getEmail(),
                getCustomerPortal(paymentMethod.getCustomer()), "payment_method.updated");
    }

    private Object processSubscriptionCreated(Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("Subscription is null in customer.subscription.created event");
        }
        com.cyna.subscriptions.models.Subscription newSubscription = null;
        try {
            newSubscription = com.cyna.subscriptions.models.Subscription.builder()
                    .subscriptionId(subscription.getId())
                    .customerId(subscription.getCustomer())
                    .productId(Long.valueOf(Product.retrieve(subscription.getItems().getData().getFirst().getPrice().getProduct()).getMetadata().get("productId")))
                    .status(SubscriptionListParams.Status.valueOf(subscription.getStatus().toUpperCase()))
                    .paymentMethod(subscription.getDefaultPaymentMethod())
                    .amount(Price.retrieve(subscription.getItems().getData().getFirst().getPrice().getId()).getUnitAmount())
                    .quantity(subscription.getItems().getData().getFirst().getQuantity())
                    .createdAt(LocalDateTime.now())
                    .createdAt(LocalDateTime.now())
                    .build();
        } catch (StripeException e) {
            log.error("[StripeService][processSubscriptionCreated] Error while processing subscription creation", e);
            throw new RuntimeException(e);
        }
        return subscriptionService.create(newSubscription);
    }

    private void processSubscriptionUpdated(Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("Subscription is null in subscription update event");
        }
        com.cyna.subscriptions.models.Subscription updatedSubscription = com.cyna.subscriptions.models.Subscription.builder()
                .status(SubscriptionListParams.Status.valueOf(subscription.getStatus()))
                .amount(subscription.getBillingCycleAnchor())
                .quantity(subscription.getItems().getData().getFirst().getQuantity())
                .build();
        mailerSendService.sendEmail(subscription.getCustomerObject().getEmail(),
                getCustomerPortal(subscription.getCustomer()), "customer.subscription.updated");
        subscriptionService.update(updatedSubscription);
    }

    private void processSetupIntentSucceeded(Subscription subscription) {
        if (subscription == null) {
            throw new NullPointerException("Subscription is null in setup_intent.succeeded event");
        }
        com.cyna.subscriptions.models.Subscription updatedSubscription = com.cyna.subscriptions.models.Subscription.builder()
                .paymentMethod(subscription.getDefaultPaymentMethod())
                .build();
        subscriptionService.update(updatedSubscription);
    }

    public String getCustomerPortal(String customerId) {
        SessionCreateParams params = SessionCreateParams.builder()
                .setCustomer(customerId)
                .setReturnUrl(stripeReturnUrl)
                .build();
        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public void updateUser(UserDto userDto){
        try {
            // Récupération de la requête entrante via RequestContextHolder
            String authHeader = this.getTokenRelay();

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String result = restClientBuilder.build()
                        .patch()
                        .uri(this.getServiceURI(AUTH_USERS_ID) + "/api/v1/user/"+userDto.getId())
                        .header(HttpHeaders.AUTHORIZATION, authHeader)
                        .body(userDto)
                        .retrieve()
                        .body(String.class);

                log.info("[StripeService][updateUser] result {}", result);
            }
        } catch (Exception e) {
            log.error("[StripeService][updateCustomerId] Error while updating customerId", e);
        }
    }

    public String getServiceURI(String serviceId){
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            log.error("No instances found for service: {}", serviceId);
            throw new RuntimeException("Auth-users service not available");
        }

        ServiceInstance serviceInstance = instances.getFirst();
        log.debug("Calling auth service at: {}", serviceInstance.getUri());

        return serviceInstance.getServiceId();
    }

    public String getTokenRelay(){
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            return currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
        }
        return null;
    }

    public String addPaymentMethod(PaymentMethodDto paymentMethodDto) {

        PaymentMethodCreateParams params = PaymentMethodCreateParams.builder()
                .setType(PaymentMethodCreateParams.Type.valueOf(paymentMethodDto.getType()))
                .setCard(
                        PaymentMethodCreateParams.CardDetails.builder()
                                .setNumber(String.valueOf(paymentMethodDto.getNumber()))
                                .setExpMonth(paymentMethodDto.getMonth())
                                .setExpYear(paymentMethodDto.getYear())
                                .setCvc(String.valueOf(paymentMethodDto.getCvc()))
                                .build()
                )
                .build();
        try {
            PaymentMethod paymentMethod = PaymentMethod.create(params);
            // Mettre à jour le client pour définir cette PaymentMethod par défaut pour la facturation
            CustomerUpdateParams.InvoiceSettings invoiceSettings = CustomerUpdateParams.InvoiceSettings.builder()
                    .setDefaultPaymentMethod(paymentMethod.getId())
                    .build();

            CustomerUpdateParams updateParams = CustomerUpdateParams.builder()
                    .setInvoiceSettings(invoiceSettings)
                    .build();

            Customer customer = Customer.retrieve(paymentMethodDto.getCustomerId());
            customer.update(updateParams);

        } catch (StripeException e) {
            log.error("[StripeService][addMethodPayment] Error while adding a payment method to customerID : {}", paymentMethodDto.getCustomerId(), e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
        return null;
    }

    public String getEphemeralKey(String customerId) {
        EphemeralKeyCreateParams params = EphemeralKeyCreateParams.builder()
                .setCustomer(customerId)
                .setStripeVersion(stripeApiVersion)
                .build();
        try {
            EphemeralKey ephemeralKey = EphemeralKey.create(params);
            return ephemeralKey.getSecret();
        } catch (StripeException e) {
            log.error("[StripeService][getEphemeralKey] Error while creating ephemeral key for customerID : {}", customerId, e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }
}
