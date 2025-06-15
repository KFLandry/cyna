package com.cyna.subscriptions.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentMethodDto {
/*Stripe*/
//Identifiant Stripe du customer //
private String customerId;
// *Identifiant Stripe du PaymentMethod (token) *//
private String paymentMethodId;
}

