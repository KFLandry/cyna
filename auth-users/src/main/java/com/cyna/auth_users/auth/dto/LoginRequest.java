package com.cyna.auth_users.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "L'email est obligatoire")
    @Email(message ="Email should be valid")
    private String email;

    @NotNull(message = "Le mot de passe est obligatoire")
    private String password;
}

