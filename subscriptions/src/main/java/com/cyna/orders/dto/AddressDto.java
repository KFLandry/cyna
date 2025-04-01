package com.cyna.orders.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressDto {
    private String customer_Id;

    private String name;

    @Pattern(regexp = "^(0[1-9]|[1-8]\\d|9[0-5])\\d{3}$",
            message = "Le code postal doit comporter 5 chiffres et être valide")
    private String postcode;

    private String city;

    private String country;
}
