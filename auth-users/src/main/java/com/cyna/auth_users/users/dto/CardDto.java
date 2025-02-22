package com.cyna.auth_users.users.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CardDto {
    @NotNull(message = "Le nom du titulaire est obligatoire")
    private String owner;

    @Pattern(regexp = "^[0-9]{16}$", message = "Le numéro doit comporter exactement 16 chiffres")
    private String number;

    @Pattern(regexp = "^(0[1-9]|1[0-2])/\\d{4}$",
            message = "La date d'expiration doit être au format mm/aaaa")
    private String expirationDate;

    @NotNull(message = "L'identifiant de l'utilisateur est obligatoire")
    private Long user_id;
}
