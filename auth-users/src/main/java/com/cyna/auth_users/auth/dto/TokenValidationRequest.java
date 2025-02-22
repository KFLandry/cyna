package com.cyna.auth_users.auth.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TokenValidationRequest {
    @NotNull(message = "Le token en vide")
    private String token;
}