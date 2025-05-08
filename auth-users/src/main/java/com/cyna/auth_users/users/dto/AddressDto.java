package com.cyna.auth_users.users.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AddressDto {
    private Long id;

    @NotBlank(message = "Le nom de l'adresse est obligatoire")
    private String name;

    @Pattern(
            regexp = "^(0[1-9]|[1-8]\\d|9[0-5])\\d{3}$",
            message = "Le code postal doit comporter 5 chiffres et être valide"
    )
    private String postcode;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotBlank(message = "Le pays est obligatoire")
    private String country;

    private Long userId;
    private String customerId;
    private String url;
}
