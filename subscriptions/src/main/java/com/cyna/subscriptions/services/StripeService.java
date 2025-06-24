package com.cyna.subscriptions.services;

import com.cyna.subscriptions.dto.*;
import com.google.gson.JsonSyntaxException;
import com.stripe.param.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.billingportal.Session;
import com.stripe.net.Webhook;
import com.stripe.param.billingportal.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.param.PaymentMethodListParams;
import org.springframework.http.ResponseEntity;



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
            UserDto userDto = UserDto.builder()
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

            case PAY_AS_YOU_GO -> PriceCreateParams.builder()
                    .setCurrency(priceDto.getCurrency())
                    .setUnitAmount(priceDto.getAmount())
                    .setRecurring(PriceCreateParams.Recurring.builder()
                            .setInterval(PriceCreateParams.Recurring.Interval.MONTH)
                            .setUsageType(PriceCreateParams.Recurring.UsageType.METERED)
                            .build())
                    .setProductData(PriceCreateParams.ProductData.builder()
                            .setName(priceDto.getProductName())
                            .putMetadata("productId", priceDto.getProductId())
                            .build())
                    .build();


            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported pricing model: " + priceDto.getPricingModel()
            );
        };

        try {
            Price stripePrice = Price.create(params);
            priceDto.setPriceId(stripePrice.getId());
            return priceDto;
        } catch (StripeException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    e.getStripeError().getMessage()
            );
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
                .setPaymentBehavior(SubscriptionCreateParams.PaymentBehavior.DEFAULT_INCOMPLETE)
                .addAllExpand(Collections.singletonList("latest_invoice.payment_intent"))
                .build();
        try {
            Subscription subscription = Subscription.create(params);

            // Sauvegarde en BDD
            try {
                // Pour récupérer le nom du produit et le modèle de tarification
                Price stripePrice = Price.retrieve(subscription.getItems().getData().getFirst().getPrice().getId());
                Product stripeProduct = Product.retrieve(stripePrice.getProduct());

                log.info("[StripeService][createSubscription] Tentative de sauvegarde de l'abonnement avec l'ID Stripe : {}", subscription.getId());
                log.info("[StripeService][createSubscription] Customer ID: {}, Price ID: {}, Product ID: {}",
                        subscription.getCustomer(), stripePrice.getId(), stripeProduct.getId());

                // Construire l'entité Subscription pour la sauvegarde en BDD
                com.cyna.subscriptions.models.Subscription newSubscription = com.cyna.subscriptions.models.Subscription.builder()
                        .subscriptionId(subscription.getId())
                        .customerId(subscription.getCustomer())
                        .productId(Long.valueOf(stripeProduct.getMetadata().get("productId")))
                        .priceId(stripePrice.getId())
                        .status(SubscriptionListParams.Status.valueOf(subscription.getStatus().toUpperCase()))
                        .paymentMethod(subscription.getDefaultPaymentMethod() != null ? subscription.getDefaultPaymentMethod() : "unknown")
                        .amount(stripePrice.getUnitAmount() != null ? stripePrice.getUnitAmount().doubleValue() / 100.0 : 0.0)
                        .quantity(subscription.getItems().getData().getFirst().getQuantity())
                        .orderNumber("ORDER-" + System.currentTimeMillis())
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build();

                // Sauvegarde l'abonnement dans la BDD
                com.cyna.subscriptions.models.Subscription savedSubscription = subscriptionService.create(newSubscription);

            } catch (Exception e) {
                log.error("[StripeService][createSubscription] Erreur lors de la sauvegarde de l'abonnement en base de données (l'abonnement Stripe a été créé): {}", e.getMessage(), e);
            }

            Map<String, Object> responseData = new HashMap<>();
            Invoice latestInvoice = Invoice.retrieve(subscription.getLatestInvoice());
            responseData.put("customerId", subscription.getCustomer());
            responseData.put("clientSecret", PaymentIntent.retrieve(latestInvoice.getPaymentIntent()).getClientSecret());
            responseData.put("subscriptionId", subscription.getId());
            return StripeObject.PRETTY_PRINT_GSON.toJson(responseData);

        } catch (StripeException e) {
            log.error("[StripeService][createSubscription] Erreur Stripe lors de la création de l'abonnement: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getStripeError().getMessage());
        }
    }

    public Map<String, String> getConfig() {
        Map<String, String> config = new HashMap<>();
        config.put("publishableKey", stripePublishableKey);
        return config;
    }


    public String cancelSubscription(String subscriptionId) {
        try {
            Subscription subscription = Subscription.retrieve(subscriptionId);
            subscription.cancel();
            return "Subscription canceled successfully";
        } catch (StripeException e) {
            String message = e.getStripeError() != null
                    ? e.getStripeError().getMessage()
                    : "Erreur Stripe inconnue lors de l'annulation.";
            log.error("[StripeService][cancelSubscription] StripeException", e);
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), message);
        }
    }



    public Object handleWebhook(String sigHeader, String payload) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookSecret);
            log.info("[Webhook] Type reçu : {}", event.getType());
            log.debug("[Webhook] Payload : {}", payload);

        } catch (SignatureVerificationException | JsonSyntaxException e) {
            throw new ResponseStatusException(HttpStatusCode.valueOf(400), e.getMessage());
        }

        StripeObject stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeObject == null) {
            log.warn("[Webhook] Impossible de désérialiser l’objet Stripe : {}", event.getType());
            return ResponseEntity.status(200).body("ignored");
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
                processCustomerDeleted((Customer) stripeObject);
                break;
            case "customer.updated":
                processCustomerUpdated((Customer) stripeObject);
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
                break;
            default:

        }
        return null;
    }

    private void processCustomerUpdated(Customer customer) {
        Address address = customer.getAddress();
        AddressDto addressDto = AddressDto.builder()
                .customer_Id(customer.getId())
                .name(address.getLine1())
                .city(address.getCity())
                .country(address.getCountry())
                .postcode(address.getPostalCode())
                .build();

        String tokenRelayed = this.getTokenRelay();
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
        // customerId à NULL
        UserDto userDto = UserDto.builder()
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
            throw new NullPointerException("Subscription est null dans l'événement customer.subscription.created");
        }
        com.cyna.subscriptions.models.Subscription newSubscription = null;
        try {
            // Si l'événement ne contient pas toutes les données, appel Stripe API.
            // Ici, je récupère Price et Product pour s'assurer d'avoir toutes les métadonnées.
            Price stripePrice = Price.retrieve(subscription.getItems().getData().getFirst().getPrice().getId());
            Product stripeProduct = Product.retrieve(stripePrice.getProduct());

            newSubscription = com.cyna.subscriptions.models.Subscription.builder()
                    .subscriptionId(subscription.getId())
                    .customerId(subscription.getCustomer())
                    // Vérif si "productId" est bien dans les métadonnées de ton produit Stripe
                    .productId(Long.valueOf(stripeProduct.getMetadata().get("productId")))
                    .status(SubscriptionListParams.Status.valueOf(subscription.getStatus().toUpperCase()))
                    // Le defaultPaymentMethod peut être null si l'abonnement est créé sans mode de paiement par défaut défini.
                    // Il est souvent mis à jour lors d'événements ultérieurs comme 'invoice.paid' ou 'payment_method.attached'.
                    .paymentMethod(subscription.getDefaultPaymentMethod() != null ? subscription.getDefaultPaymentMethod() : "unknown")
                    // amount de Price est en centimes. Convertis en double pour la BDD.
                    .amount(stripePrice.getUnitAmount() != null ? stripePrice.getUnitAmount().doubleValue() / 100.0 : 0.0)
                    .quantity(subscription.getItems().getData().getFirst().getQuantity())
                    .orderNumber("WEBHOOK-ORDER-" + System.currentTimeMillis()) // Générer d'un orderNumber pour les webhooks
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now()) // Ajout de updatedAt
                    .build();

            log.info("[StripeService][processSubscriptionCreated] Tentative de sauvegarde de l'abonnement via webhook pour l'ID Stripe: {}", subscription.getId());
            com.cyna.subscriptions.models.Subscription savedSubscription = subscriptionService.create(newSubscription);
            log.info("[StripeService][processSubscriptionCreated] Abonnement sauvegardé avec succès en base de données via webhook avec l'ID interne: {}", savedSubscription.getId());

        } catch (StripeException e) {
            log.error("[StripeService][processSubscriptionCreated] Erreur Stripe lors du traitement de la création d'abonnement via webhook: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur lors de la récupération des détails Stripe: " + e.getMessage());
        } catch (Exception e) {
            log.error("[StripeService][processSubscriptionCreated] Erreur générique lors du traitement de la création d'abonnement via webhook: {}", e.getMessage(), e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne lors du traitement du webhook: " + e.getMessage());
        }
        return newSubscription; // Je retourne l'objet pour confirmation du webhook
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

    public void updateUser(UserDto userDto) {
        try {
            // Récupération de la requête entrante via RequestContextHolder
            String authHeader = this.getTokenRelay();

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String result = restClientBuilder.build()
                        .patch()
                        .uri(this.getServiceURI(AUTH_USERS_ID) + "/api/v1/user/" + userDto.getId())
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

    public URI getServiceURI(String serviceId) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        if (instances.isEmpty()) {
            log.error("No instances found for service: {}", serviceId);
            throw new RuntimeException("Auth-users service not available");
        }

        ServiceInstance serviceInstance = instances.getFirst();
        log.debug("Calling auth service at: {}", serviceInstance.getUri());

        return serviceInstance.getUri();
    }

    public String getTokenRelay() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest currentRequest = attributes.getRequest();
            return currentRequest.getHeader(HttpHeaders.AUTHORIZATION);
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

    /*Stripe method*/
    public void attachPaymentMethod(PaymentMethodDto dto) {
        if (dto.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "customerId is required");
        }
        try {
            // 1) Récupérer le PaymentMethod
            PaymentMethod pm = PaymentMethod.retrieve(dto.getPaymentMethodId());

            // 2) Attacher au customer
            pm = pm.attach(
                    PaymentMethodAttachParams.builder()
                            .setCustomer(dto.getCustomerId())
                            .build()
            );

            // 3) Mettre à jour les invoice_settings du customer
            Customer customer = Customer.retrieve(dto.getCustomerId());
            CustomerUpdateParams params = CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(pm.getId())
                                    .build()
                    )
                    .build();
            customer.update(params);

        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getStripeError().getMessage());
        }
    }

    /**
     * Liste toutes les méthodes de paiement d'un customer Stripe.
     *
     * @param customerId l'ID Stripe du customer
     */
    public List<PaymentMethodResponseDto> listPaymentMethods(String customerId) {
        try {
            // Récupération des PM Stripe
            PaymentMethodListParams params = PaymentMethodListParams.builder()
                    .setCustomer(customerId)
                    .setType(PaymentMethodListParams.Type.CARD)
                    .build();
            PaymentMethodCollection pms = PaymentMethod.list(params);

            // Customer pour default
            Customer customer = Customer.retrieve(customerId);
            String defaultPm = customer.getInvoiceSettings().getDefaultPaymentMethod();

            // Mapping
            return pms.getData().stream()
                    .map(pm -> PaymentMethodResponseDto.builder()
                            .id(pm.getId())
                            .last4(pm.getCard().getLast4())
                            .expiryMonth(pm.getCard().getExpMonth().intValue())
                            .expiryYear(pm.getCard().getExpYear().intValue())
                            .type(pm.getCard().getBrand())
                            .cardholderName(pm.getBillingDetails().getName())
                            .isDefault(pm.getId().equals(defaultPm))
                            .build()
                    )
                    .collect(Collectors.toList());
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }

    }

    public void detachPaymentMethod(String paymentMethodId) {
        try {
            //1.Récupérer la PM
            PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);

            //2. Détacher la PM du customer
            pm.detach();


        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    public void setDefaultPaymentMethod(String paymentMethodId, String customerId) {
        try {
            // 1) Attacher éventuellement la PM si ce n’est pas déjà fait
            PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
            pm.attach(PaymentMethodAttachParams.builder()
                    .setCustomer(customerId)
                    .build()
            );
            // 2) Mettre à jour invoice_settings
            Customer customer = Customer.retrieve(customerId);
            customer.update(CustomerUpdateParams.builder()
                    .setInvoiceSettings(
                            CustomerUpdateParams.InvoiceSettings.builder()
                                    .setDefaultPaymentMethod(paymentMethodId)
                                    .build()
                    )
                    .build()
            );
        } catch (StripeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        }
    }

    /**
     * Récupère la liste des abonnements Stripe pour un customerId donné.
     *
     * @param customerId l'ID Stripe du customer
     * @return une liste de SubscriptionDto représentant les abonnements du client
     */
    public List<SubscriptionDto> listSubscriptionsByCustomer(String customerId) {
        try {
            SubscriptionListParams params = SubscriptionListParams.builder()
                    .setCustomer(customerId)
                    .setStatus(SubscriptionListParams.Status.ACTIVE)
                    .build();

            SubscriptionCollection subscriptions = Subscription.list(params);

            // Mapper les objets Stripe Subscription en SubscriptionDto
            return subscriptions.getData().stream()
                    .map(this::mapStripeSubscriptionToDto)
                    .collect(Collectors.toList());

        } catch (StripeException e) {
            log.error("[StripeService][listSubscriptionsByCustomer] Erreur lors de la récupération des abonnements pour le client {}: {}", customerId, e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getStripeError().getMessage(), e);
        }
    }

    /**
     * Méthode utilitaire pour mapper un objet Stripe Subscription à un SubscriptionDto.
     *
     * @param stripeSubscription l'objet Subscription de Stripe
     * @return un SubscriptionDto
     */
    private SubscriptionDto mapStripeSubscriptionToDto(Subscription stripeSubscription) {
        // Stripe fournit un timestamp en secondes donc  convertion en millisecondes pour JS
        Long createdAtMillis = stripeSubscription.getCreated() * 1_000L;

        Long amount = null;
        String productName = null;
        String pricingModel = null;
        String priceId = null;

        if (stripeSubscription.getItems() != null && !stripeSubscription.getItems().getData().isEmpty()) {
            SubscriptionItem subscriptionItem = stripeSubscription.getItems().getData().getFirst();
            if (subscriptionItem.getPrice() != null) {
                Price price = subscriptionItem.getPrice();
                amount = price.getUnitAmount();
                priceId = price.getId();

                try {
                    Product product = Product.retrieve(price.getProduct());
                    productName = product.getName();
                    pricingModel = product.getMetadata().get("pricingModel");
                } catch (StripeException e) {
                    log.warn("Impossible de récupérer les détails du produit pour le prix {}: {}", price.getId(), e.getMessage());
                }
            }
        }

        return SubscriptionDto.builder()
                .subscriptionId(stripeSubscription.getId())
                .customerId(stripeSubscription.getCustomer())
                .priceId(priceId)
                .status(stripeSubscription.getStatus())
                .quantity(
                        stripeSubscription.getItems().getData().isEmpty()
                                ? null
                                : stripeSubscription.getItems().getData().getFirst().getQuantity()
                )
                .amount(amount)
                .productName(productName)
                .pricingModel(pricingModel)
                .createdAt(createdAtMillis)
                .build();
    }
}